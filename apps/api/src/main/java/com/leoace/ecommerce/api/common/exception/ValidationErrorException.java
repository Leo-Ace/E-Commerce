package com.leoace.ecommerce.api.common.exception;

public class ValidationErrorException extends BusinessException {

    public ValidationErrorException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }
}
