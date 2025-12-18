package com.KimZo2.Back.domain.auth.dto;

import lombok.Getter;

@Getter
public class NaverDTO {
    private String resultcode;
    private String message;
    private NaverUser response;

    @Getter
    public  static class NaverUser {
        private String id;
        private String name;
        private String birthday;
    }
}
