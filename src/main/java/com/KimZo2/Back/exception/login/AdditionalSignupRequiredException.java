package com.KimZo2.Back.exception.login;

public class AdditionalSignupRequiredException extends RuntimeException {
    private final String provider;
    private final String providerId;

    public AdditionalSignupRequiredException(String provider, String providerId) {
        super("추가 회원가입이 필요한 사용자입니다.");
        this.provider = provider;
        this.providerId = providerId;
    }

    public String getProvider() { return provider; }
    public String getProviderId() { return providerId; }
}
