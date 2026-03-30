package com.leoace.ecommerce.api.domain.user.service;

import com.leoace.ecommerce.api.common.exception.BusinessException;
import com.leoace.ecommerce.api.common.exception.ErrorCode;
import com.leoace.ecommerce.api.domain.user.entity.RefreshToken;
import com.leoace.ecommerce.api.domain.user.entity.Role;
import com.leoace.ecommerce.api.domain.user.entity.User;
import com.leoace.ecommerce.api.domain.user.enums.UserStatus;
import com.leoace.ecommerce.api.domain.user.model.request.LoginRequest;
import com.leoace.ecommerce.api.domain.user.model.request.RefreshTokenRequest;
import com.leoace.ecommerce.api.domain.user.model.request.RegisterRequest;
import com.leoace.ecommerce.api.domain.user.model.response.AuthTokenResponse;
import com.leoace.ecommerce.api.domain.user.model.response.CurrentUserResponse;
import com.leoace.ecommerce.api.domain.user.repository.RefreshTokenRepository;
import com.leoace.ecommerce.api.domain.user.repository.RoleRepository;
import com.leoace.ecommerce.api.domain.user.repository.UserRepository;
import com.leoace.ecommerce.api.security.CustomUserPrincipal;
import com.leoace.ecommerce.api.security.JwtProperties;
import com.leoace.ecommerce.api.security.JwtService;
import com.leoace.ecommerce.api.security.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String DEFAULT_BUYER_ROLE = "BUYER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenGenerator tokenGenerator;

    @Transactional
    public AuthTokenResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        String email = normalizeEmail(request.getEmail());
        String fullName = normalizeRequiredText(request.getFullName(), "Full name is required");
        String phone = normalizeOptionalText(request.getPhone());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Email already exists");
        }

        if (StringUtils.hasText(phone) && userRepository.existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Phone already exists");
        }

        Role buyerRole = roleRepository.findByCodeAndIsActiveTrue(DEFAULT_BUYER_ROLE)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "Default BUYER role is not configured"
                ));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setIsActive(true);
        user.setLastLoginAt(Instant.now());
        user.getRoles().add(buyerRole);

        userRepository.save(user);

        User savedUser = userRepository.findAuthById(user.getId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "Failed to load registered user"
                ));

        return issueTokens(savedUser, ipAddress, userAgent);
    }

    @Transactional
    public AuthTokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = normalizeEmail(request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findAuthByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid credentials"));

        ensureUserCanAuthenticate(user);

        user.setLastLoginAt(Instant.now());

        return issueTokens(user, ipAddress, userAgent);
    }

    @Transactional
    public AuthTokenResponse refresh(RefreshTokenRequest request, String ipAddress, String userAgent) {
        String rawRefreshToken = request.getRefreshToken();
        String tokenHash = tokenGenerator.sha256(rawRefreshToken);

        RefreshToken existingToken = refreshTokenRepository.findWithUserByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (existingToken.isRevoked()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh token has been revoked");
        }

        if (existingToken.isExpired()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh token has expired");
        }

        User user = existingToken.getUser();
        ensureUserCanAuthenticate(user);

        existingToken.setRevokedAt(Instant.now());
        existingToken.setLastUsedAt(Instant.now());

        String newRawRefreshToken = tokenGenerator.generateRefreshToken();
        RefreshToken newRefreshToken = buildRefreshToken(
                user,
                newRawRefreshToken,
                ipAddress,
                userAgent
        );

        refreshTokenRepository.save(newRefreshToken);
        existingToken.setReplacedById(newRefreshToken.getId());

        return buildAuthTokenResponse(user, newRawRefreshToken);
    }

    public CurrentUserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findAuthById(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Current user not found"
                ));

        return toCurrentUserResponse(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String tokenHash = tokenGenerator.sha256(request.getRefreshToken());

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.setRevokedAt(Instant.now());
                token.setLastUsedAt(Instant.now());
            }
        });
    }

    private AuthTokenResponse issueTokens(User user, String ipAddress, String userAgent) {
        String rawRefreshToken = tokenGenerator.generateRefreshToken();
        RefreshToken refreshToken = buildRefreshToken(user, rawRefreshToken, ipAddress, userAgent);
        refreshTokenRepository.save(refreshToken);

        return buildAuthTokenResponse(user, rawRefreshToken);
    }

    private RefreshToken buildRefreshToken(
            User user,
            String rawRefreshToken,
            String ipAddress,
            String userAgent
    ) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenGenerator.sha256(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()));
        refreshToken.setIpAddress(normalizeOptionalText(ipAddress));
        refreshToken.setUserAgent(normalizeOptionalText(userAgent));
        refreshToken.setLastUsedAt(Instant.now());
        return refreshToken;
    }

    private AuthTokenResponse buildAuthTokenResponse(User user, String rawRefreshToken) {
        CustomUserPrincipal principal = CustomUserPrincipal.from(user);
        String accessToken = jwtService.generateAccessToken(principal);

        return AuthTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtService.getAccessTokenExpirationSeconds())
                .user(toCurrentUserResponse(user))
                .build();
    }

    private CurrentUserResponse toCurrentUserResponse(User user) {
        Set<String> roles = new LinkedHashSet<>();
        Set<String> permissions = new LinkedHashSet<>();

        user.getRoles().forEach(role -> {
            if (Boolean.TRUE.equals(role.getIsActive())) {
                roles.add(role.getCode());

                role.getPermissions().forEach(permission -> {
                    if (Boolean.TRUE.equals(permission.getIsActive())) {
                        permissions.add(permission.getCode());
                    }
                });
            }
        });

        return CurrentUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .isActive(user.getIsActive())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    private void ensureUserCanAuthenticate(User user) {
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Account is inactive");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Account is locked");
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Account is disabled");
        }
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}