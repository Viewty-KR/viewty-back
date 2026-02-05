package com.viewty.viewtyback.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "ID는 필수입니다")
    @Size(min = 3, max = 30, message = "사용자는 3~30자여야 합니다")
    private String userId;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 3, message = "비밀번호는 3자 이상이어야 합니다")
    private String password;

    private String name;

}
