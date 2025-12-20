package com.KimZo2.Back.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;

public class GoogleDTO {

    @Getter
    @Setter
    public static class GoogleUser {
        private String id;
    }
}
