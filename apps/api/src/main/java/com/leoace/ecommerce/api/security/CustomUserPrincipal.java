package com.leoace.ecommerce.api.security;

import com.leoace.ecommerce.api.domain.user.entity.Permission;
import com.leoace.ecommerce.api.domain.user.entity.Role;
import com.leoace.ecommerce.api.domain.user.entity.User;
import com.leoace.ecommerce.api.domain.user.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Builder
public class CustomUserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String password;
    private final String fullName;
    private final String phone;
    private final UserStatus status;
    private final Boolean emailVerified;
    private final Boolean phoneVerified;
    private final Boolean isActive;
    private final Set<String> roleCodes;
    private final Set<String> permissionCodes;
    private final Collection<? extends GrantedAuthority> authorities;

    public static CustomUserPrincipal from(User user) {
        Set<String> roleCodes = new LinkedHashSet<>();
        Set<String> permissionCodes = new LinkedHashSet<>();
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (Role role : user.getRoles()) {
            if (!Boolean.TRUE.equals(role.getIsActive())) {
                continue;
            }

            roleCodes.add(role.getCode());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));

            for (Permission permission : role.getPermissions()) {
                if (!Boolean.TRUE.equals(permission.getIsActive())) {
                    continue;
                }

                permissionCodes.add(permission.getCode());
                authorities.add(new SimpleGrantedAuthority(permission.getCode()));
            }
        }

        return CustomUserPrincipal.builder()
                .id(user.getId())
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .isActive(user.getIsActive())
                .roleCodes(roleCodes)
                .permissionCodes(permissionCodes)
                .authorities(authorities)
                .build();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isActive) && status != UserStatus.DISABLED;
    }
}