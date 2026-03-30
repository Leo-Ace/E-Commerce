package com.leoace.ecommerce.api.domain.user.model.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class CurrentUserResponse {

    private final UUID id;
    private final String email;
    private final String fullName;
    private final String phone;
    private final String status;
    private final Boolean emailVerified;
    private final Boolean phoneVerified;
    private final Boolean isActive;
    private final Set<String> roles;
    private final Set<String> permissions;
}
