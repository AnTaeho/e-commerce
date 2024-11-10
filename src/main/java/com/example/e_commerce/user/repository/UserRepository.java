package com.example.e_commerce.user.repository;

import com.example.e_commerce.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

}
