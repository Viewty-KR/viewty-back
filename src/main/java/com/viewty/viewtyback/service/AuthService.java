package com.viewty.viewtyback.service;

import com.viewty.viewtyback.dto.request.LoginRequest;
import com.viewty.viewtyback.dto.request.PwdRequest;
import com.viewty.viewtyback.dto.request.SignupRequest;
import com.viewty.viewtyback.dto.request.SurveyRequest;
import com.viewty.viewtyback.dto.response.SurveyResponse;
import com.viewty.viewtyback.dto.response.TokenResponse;
import com.viewty.viewtyback.dto.response.UserResponse;
import com.viewty.viewtyback.entity.User;
import com.viewty.viewtyback.exception.CustomException;
import com.viewty.viewtyback.exception.ErrorCode;
import com.viewty.viewtyback.repository.UserRepository;
import com.viewty.viewtyback.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new CustomException(ErrorCode.DUPLICATE_USERID);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        String token = jwtProvider.createToken(user.getUserId(), user.getName(), user.getRole());

        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    public SurveyResponse surveySave(SurveyRequest request, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String finalSkinType = resolveFinalSkinType(request.getSkinType(), request.getFeelingAfterWash(), request.getAfternoonSkin(), request.getPoreSize());
        String concernsString = String.join(",", request.getConcerns());
        user.updateSurvey(concernsString, request.getSensitivity(), finalSkinType, request.getFeelingAfterWash(), request.getAfternoonSkin(), request.getPoreSize());
        return SurveyResponse.from(user);
    }

    public UserResponse getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse pwdUpdate(PwdRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updatePasswordSurvey(passwordEncoder.encode(request.getPassword()));
        return UserResponse.from(user);
    }

    private String resolveFinalSkinType(String SkinType, String FeelingAfterWash, String afternoonSkin, String poreSize) {
        String finalSkinType = SkinType;

        if ("D".equals(finalSkinType)) {
            java.util.Map<String, Integer> counts = new java.util.HashMap<>();
            counts.put("A", 0);
            counts.put("B", 0);
            counts.put("C", 0);

            String[] values = {
                    FeelingAfterWash,
                    afternoonSkin,
                    poreSize
            };

            for (String v : values) {
                if ("A".equals(v)) counts.put("A", counts.get("A") + 1);
                else if ("B".equals(v)) counts.put("B", counts.get("B") + 1);
                else if ("C".equals(v)) counts.put("C", counts.get("C") + 1);
            }

            int countA = counts.get("A");
            int countB = counts.get("B");
            int countC = counts.get("C");

            if (countA > countB && countA > countC) {
                finalSkinType = "A";
            } else if (countB > countA && countB > countC) {
                finalSkinType = "B";
            } else if (countC > countA && countC > countB) {
                finalSkinType = "C";
            } else {
                finalSkinType = "B";
            }
        }

        return finalSkinType;
    }
}