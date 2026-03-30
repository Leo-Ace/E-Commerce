package com.leoace.ecommerce.api.domain.user.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresInSeconds;
    private final CurrentUserResponse user;
}
