package com.viewty.viewtyback.controller;

import com.viewty.viewtyback.dto.request.LoginRequest;
import com.viewty.viewtyback.dto.request.PwdRequest;
import com.viewty.viewtyback.dto.request.SignupRequest;
import com.viewty.viewtyback.dto.request.SurveyRequest;
import com.viewty.viewtyback.dto.response.*;
import com.viewty.viewtyback.security.CustomUserDetails;
import com.viewty.viewtyback.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        UserResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/survey")
    public ResponseEntity<ApiResponse<SurveyResponse>> survey(
            @Valid @RequestBody SurveyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = (request.getUserId() == null || request.getUserId().isEmpty()) ? userDetails.getUserId() : request.getUserId();
        SurveyResponse response = authService.surveySave(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> profile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long id = userDetails.getId();
        UserResponse response =  authService.getUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/pwdUpdate")
    public ResponseEntity<ApiResponse<UserResponse>> pwdUpdate(
            @Valid @RequestBody PwdRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        UserResponse response = authService.pwdUpdate(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
