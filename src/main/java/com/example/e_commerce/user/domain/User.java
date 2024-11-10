package com.example.e_commerce.user.domain;

import com.example.e_commerce.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String name;

    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private User(String name, String email, String password, UserRole userRole) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    public static User user(String name, String email, String password) {
        return new User(name, email, password, UserRole.USER);
    }

    public static User seller(String name, String email, String password) {
        return new User(name, email, password, UserRole.SELLER);
    }

    public static User admin(String name, String email, String password) {
        return new User(name, email, password, UserRole.ADMIN);
    }
}
