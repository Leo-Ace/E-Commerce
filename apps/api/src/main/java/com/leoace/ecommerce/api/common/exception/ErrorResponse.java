package com.leoace.ecommerce.api.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final boolean success;
    private final String code;
    private final String message;
    private final String path;
    private final Map<String ,String> fieldErrors;
    private final Instant timestamp;
}
