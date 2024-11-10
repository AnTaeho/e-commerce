package com.example.e_commerce.user.service;

import com.example.e_commerce.user.controller.dto.JoinRequest;
import com.example.e_commerce.user.controller.dto.LoginRequest;
import com.example.e_commerce.user.controller.dto.UserResponse;
import com.example.e_commerce.user.domain.User;
import com.example.e_commerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse joinUser(JoinRequest joinRequest) {
        checkEmail(joinRequest.email());
        User user = User.user(
                joinRequest.username(),
                joinRequest.email(),
                joinRequest.password()
        );
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser.getId());
    }

    public UserResponse joinSeller(JoinRequest joinRequest) {
        checkEmail(joinRequest.email());
        User user = User.seller(
                joinRequest.username(),
                joinRequest.email(),
                joinRequest.password()
        );
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser.getId());
    }

    public UserResponse joinAdmin(JoinRequest joinRequest) {
        checkEmail(joinRequest.email());
        User user = User.admin(
                joinRequest.username(),
                joinRequest.email(),
                joinRequest.password()
        );
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser.getId());
    }

    private void checkEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    public UserResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .stream()
                .filter(it -> it.getPassword().equals(loginRequest.password()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        return new UserResponse(user.getId());
    }


}
