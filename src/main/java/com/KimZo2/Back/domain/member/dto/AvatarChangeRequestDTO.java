package com.KimZo2.Back.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvatarChangeRequestDTO {

    @NotBlank(message = "Avatar Number는 필수입니다.")
    private int avatar;
}
