package com.viewty.viewtyback.dto.request;

import java.util.List;

import lombok.Getter;

@Getter
public class SurveyRequest {
    private String userId;
    private List<String> concerns;
    private String sensitivity;
    private String skinType;
    private String feelingAfterWash;
    private String afternoonSkin;
    private String poreSize;
}
