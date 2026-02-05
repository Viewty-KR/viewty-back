package com.viewty.viewtyback.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {

    private String fileUrl;

    public static FileUploadResponse of(String fileUrl) {
        return FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .build();
    }


}
