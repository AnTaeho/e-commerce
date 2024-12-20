package com.example.e_commerce.queue;

import static com.example.e_commerce.global.QueueConst.ALREADY_ISSUED_USER;
import static com.example.e_commerce.global.QueueConst.PROCESSING;
import static com.example.e_commerce.global.QueueConst.PROCESSING_QUEUE_KEY;
import static com.example.e_commerce.global.QueueConst.WAITING;
import static com.example.e_commerce.global.QueueConst.WAITING_QUEUE_KEY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.e_commerce.queue.controller.dto.QueueResponse;
import com.example.e_commerce.queue.service.QueueCommandService;
import com.example.e_commerce.queue.service.QueueQueryService;
import com.example.e_commerce.queue.service.facade.QueueService;
import com.example.e_commerce.queue.util.QueueJwtUtil;
import com.example.e_commerce.user.domain.User;
import com.example.e_commerce.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;


@SpringBootTest
public class QueueServiceTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueCommandService queueCommandService;

    @Autowired
    private QueueQueryService queueQueryService;

    @Autowired
    private QueueJwtUtil queueJwtUtil;

    private ZSetOperations<String, String> zSet;

    private User testUser;

    @BeforeEach
    void beforeEach() {
        zSet = redisTemplate.opsForZSet();
        User user = User.user("username", "email@email.com", "password123456789");
        testUser = userRepository.save(user);
    }

    @AfterEach
    void afterEach() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("user는 대기열에 중복 등록할 수 없다.")
    void issueTokenAndDuplicateEnqueueTest() {
        //given 한 번 등록
        queueCommandService.issueTokenAndEnqueue(testUser.getId());

        //when 한번 더 등록
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> queueCommandService.issueTokenAndEnqueue(testUser.getId())
        );

        //then
        assertEquals(ALREADY_ISSUED_USER, exception.getMessage());
    }

    @Test
    @DisplayName("토큰을 발급하고 진행열이 다 차지 않은 경우 진행열에 넣는다.")
    void issueTokenAndEnqueueProcessingQueueTest() {
        //when
        String token = queueService.issueTokenAndEnqueue(testUser.getId()).token();

        //then
        String value = testUser.getId() + ":" + token;
        assertNotNull(zSet.score(PROCESSING_QUEUE_KEY, value));
        assertEquals(1L, zSet.zCard(PROCESSING_QUEUE_KEY));
    }

    @Test
    @DisplayName("토큰을 발급하고 진행열이 다 찬 경우 대기열에 넣는다.")
    void issueTokenAndEnqueueWaitingQueueTest() {
        //given

        for (int i = 1000; i < 2000; i++) {
            User savedUser = userRepository.save(User.user("username", "email@email.com", "passwordpassowrd"));
            queueCommandService.issueTokenAndEnqueue(savedUser.getId());
        }

        //when
        String token = queueService.issueTokenAndEnqueue(testUser.getId()).token();

        //then
        String value = testUser.getId() + ":" + token;
        assertNotNull(zSet.score(WAITING_QUEUE_KEY, value));
        assertEquals(1, zSet.zCard(WAITING_QUEUE_KEY));
    }

    @Test
    @DisplayName("대기열을 탈출한다.")
    void dequeueWaitingQueueTest() {
        //given
        for (int i = 1000; i < 2100; i++) {
            User savedUser = userRepository.save(User.user("username", "email@email.com", "passwordpassowrd"));
            queueCommandService.issueTokenAndEnqueue(savedUser.getId());
        }


        String token = queueService.issueTokenAndEnqueue(testUser.getId()).token();

        //when
        queueService.dequeueWaitingQueue(testUser.getId(), token);

        //then
        String value = testUser.getId().toString() + ":" + token;
        assertNull(zSet.score(WAITING_QUEUE_KEY, value));
        assertEquals(100, zSet.zCard(WAITING_QUEUE_KEY));

    }

    @Test
    @DisplayName("현재 나의 대기열 상태를 조회한다.(진행열에 들어온 상황)")
    void findQueueStatusTest1() {
        //given
        String token = queueService.issueTokenAndEnqueue(testUser.getId()).token();

        //when
        QueueResponse queueResponse = queueService.findQueueStatus(testUser.getId(), token);

        //then
        assertEquals(PROCESSING, queueResponse.status());
        assertEquals(0, queueResponse.waitingQueueCount());
    }

    @Test
    @DisplayName("현재 나의 대기열 상태를 조회한다.(대기열에 있는 상황)")
    void findQueueStatusTest2() {
        //given
        for (int i = 1000; i < 2100; i++) {
            User savedUser = userRepository.save(User.user("username", "email@email.com", "passwordpassowrd"));
            queueCommandService.issueTokenAndEnqueue(savedUser.getId());
        }

        String token = queueService.issueTokenAndEnqueue(testUser.getId()).token();

        //when
        QueueResponse queueResponse = queueQueryService.findQueueStatus(testUser.getId(), token);

        //then
        assertEquals(WAITING, queueResponse.status());
        assertEquals(101, queueResponse.waitingQueueCount());
    }

    @Test
    @DisplayName("API 수행 완료 시 진행열에서 탈출한다.")
    void completeProcessingTokenTest() {
        //given
        for (int i = 1000; i < 1500; i++) {
            User savedUser = userRepository.save(User.user("username", "email@email.com", "passwordpassowrd"));
            queueCommandService.issueTokenAndEnqueue(savedUser.getId());
        }

        //when
        String token = queueCommandService.issueTokenAndEnqueue(testUser.getId());

        //then
        assertEquals(501, zSet.zCard(PROCESSING_QUEUE_KEY));

        //when
        queueCommandService.removeTokenInProcessingQueue(testUser.getId(), token);

        //then
        assertEquals(500, zSet.zCard(PROCESSING_QUEUE_KEY));
    }

    @Test
    @DisplayName("스케줄러 실행 시 대기열 -> 진행열로 변환된다.")
    void updateWaitingToProcessingTest() {
        //given
        //처리큐에 들어갈 500명 기록
        List<String> tokens = new ArrayList<>();
        for (int i = 1000; i <= 1500; i++) {
            User savedUser = userRepository.save(User.user( "username", "email@email.com", "passwordpassowrd"));
            tokens.add(queueCommandService.issueTokenAndEnqueue(savedUser.getId()));
        }

        for (int i = 1501; i <= 2500; i++) {
            User savedUser = userRepository.save(User.user( "username", "email@email.com", "passwordpassowrd"));
            queueCommandService.issueTokenAndEnqueue(savedUser.getId());
        }
        queueCommandService.issueTokenAndEnqueue(testUser.getId());


        //when 500개 완료했을 때 스케줄러를 실행하면
        for (String token : tokens) {
            Long tokenUserId = queueJwtUtil.getUserIdByToken(token);
            queueCommandService.removeTokenInProcessingQueue(tokenUserId, token);
        }

        queueCommandService.updateWaitingToProcessing();

        //then 대기열에서 진행열으로 500개 당겨져와야한다.
        assertEquals(zSet.zCard(PROCESSING_QUEUE_KEY), 1000);
        assertEquals(zSet.zCard(WAITING_QUEUE_KEY), 1);
    }

    @Test
    @DisplayName("processing queue에 빈자리 만큼 waiting queue에서 채워준다.")
    void updateQueueTest() {
        //given
        List<String> tokens = new ArrayList<>();

        // 작업을 마치고 나갈 10명 기록
        for (int i = 1000; i < 1010; i++) {
            User savedUser = userRepository.save(User.user("username", "email@email.com", "passwordpassowrd"));
            tokens.add(queueCommandService.issueTokenAndEnqueue(savedUser.getId()));
        }

        for (int i = 1010; i < 2100-1; i++) {
            User savedUser = userRepository.save(User.user( "username", "email@email.com", "passwordpassowrd"));
            queueCommandService.issueTokenAndEnqueue(savedUser.getId());
        }

        // 마지막에 접근한 1명을 기록
        Long lastUserId = userRepository.save(User.user("username", "email@email.com", "passwordpassowrd")).getId();
        String lastUserToken = queueCommandService.issueTokenAndEnqueue(lastUserId);

        //when
        QueueResponse queueResponse1 = queueService.findQueueStatus(lastUserId, lastUserToken);

        // 초반 10명을 processing queue에서 삭제
        for (String token : tokens) {
            Long tokenUserId = queueJwtUtil.getUserIdByToken(token);
            queueCommandService.removeTokenInProcessingQueue(tokenUserId, token);
        }

        //update
        queueCommandService.updateWaitingToProcessing();
        QueueResponse queueResponse2 = queueService.findQueueStatus(lastUserId, lastUserToken);

        //then
        assertThat(queueResponse1).isNotNull();
        assertThat(queueResponse1.status()).isEqualTo(WAITING);
        assertThat(queueResponse1.waitingQueueCount()).isEqualTo(100L);
        assertThat(queueResponse1.estimatedWaitTime()).isEqualTo(0L);

        assertThat(queueResponse2).isNotNull();
        assertThat(queueResponse2.status()).isEqualTo(WAITING);
        assertThat(queueResponse2.waitingQueueCount()).isEqualTo(90L);
        assertThat(queueResponse2.estimatedWaitTime()).isEqualTo(0L);
    }
}
