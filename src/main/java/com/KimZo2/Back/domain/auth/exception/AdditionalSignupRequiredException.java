package com.KimZo2.Back.domain.auth.exception;

import com.KimZo2.Back.global.exception.CustomException;
import com.KimZo2.Back.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AdditionalSignupRequiredException extends CustomException {
    private final String provider;
    private final String providerId;

    public AdditionalSignupRequiredException(ErrorCode errorCode, String provider, String providerId) {
        super(errorCode);
        this.provider = provider;
        this.providerId = providerId;
    }
}
