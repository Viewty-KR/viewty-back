package com.viewty.viewtyback.dto.response;

import com.viewty.viewtyback.entity.User;
import com.viewty.viewtyback.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String userId;
    private String email;
    private String name;
    private UserRole role;
    private String profileImageUrl;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}