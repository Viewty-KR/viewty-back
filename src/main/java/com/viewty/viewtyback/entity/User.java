package com.viewty.viewtyback.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER; // 역할 (일반 사용자, ADMIN)

    //== 설문 정보 필드 추가 ==//
    private String concerns;
    private String sensitivity;
    private String skinType;
    private String feelingAfterWash;
    private String afternoonSkin;
    private String poreSize;

    @Builder
    public User(String userId, String email, String password, String name) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    // 비밀번호
    public void updatePasswordSurvey(String password) {
        this.password = password;
    }

    // 설문 정보
    public void updateSurvey(String concerns, String sensitivity, String skinType, String feelingAfterWash, String afternoonSkin, String poreSize) {
        this.concerns = concerns;
        this.sensitivity = sensitivity;
        this.skinType = skinType;
        this.feelingAfterWash = feelingAfterWash;
        this.afternoonSkin = afternoonSkin;
        this.poreSize = poreSize;
    }
}
