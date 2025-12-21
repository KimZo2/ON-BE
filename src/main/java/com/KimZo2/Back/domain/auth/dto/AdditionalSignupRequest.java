package com.KimZo2.Back.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdditionalSignupRequest {

    @NotBlank(message = "소셜 제공자(provider)는 필수입니다.")
    private String provider;

    @NotBlank(message = "소셜 ID(providerId)는 필수입니다.")
    private String providerId;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
    private String nickname;

    @NotBlank(message = "생년월일은 필수입니다.")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일 형식이 올바르지 않습니다. (YYYY-MM-DD)")
    private String birthday;

    @AssertTrue(message = "약관에 동의해야 가입할 수 있습니다.")
    private boolean agreement;
}
