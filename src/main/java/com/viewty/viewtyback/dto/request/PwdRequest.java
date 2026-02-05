package com.viewty.viewtyback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PwdRequest {
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 3, message = "비밀번호는 3자 이상이어야 합니다")
    private String password;
}
