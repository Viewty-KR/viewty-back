package com.viewty.viewtyback.dto.response;

import com.viewty.viewtyback.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SurveyResponse {
    private String concerns;
    private String sensitivity;
    private String skinType;
    private String feelingAfterWash;
    private String afternoonSkin;
    private String poreSize;

    public static SurveyResponse from(User user) {
        return SurveyResponse.builder()
                .concerns(user.getConcerns())
                .sensitivity(user.getSensitivity())
                .skinType(user.getSkinType())
                .feelingAfterWash(user.getFeelingAfterWash())
                .afternoonSkin(user.getAfternoonSkin())
                .poreSize(user.getPoreSize())
                .build();
    }
}
