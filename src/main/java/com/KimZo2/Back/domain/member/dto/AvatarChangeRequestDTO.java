package com.KimZo2.Back.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvatarChangeRequestDTO {

    @jakarta.validation.constraints.Min(value = 1, message = "Avatar Number는 1 이상이어야 합니다.")
    private int avatar;
}
