package com.leoace.ecommerce.api.common.exception;

public class InternalServerErrorException extends BusinessException {

    public InternalServerErrorException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
