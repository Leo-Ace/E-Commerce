package com.leoace.ecommerce.api.interfaces.rest.publicapi.auth;

import com.leoace.ecommerce.api.common.exception.BusinessException;
import com.leoace.ecommerce.api.common.exception.ErrorCode;
import com.leoace.ecommerce.api.common.response.ApiResponse;
import com.leoace.ecommerce.api.domain.user.model.request.LoginRequest;
import com.leoace.ecommerce.api.domain.user.model.request.RefreshTokenRequest;
import com.leoace.ecommerce.api.domain.user.model.request.RegisterRequest;
import com.leoace.ecommerce.api.domain.user.model.response.AuthTokenResponse;
import com.leoace.ecommerce.api.domain.user.model.response.CurrentUserResponse;
import com.leoace.ecommerce.api.domain.user.service.AuthService;
import com.leoace.ecommerce.api.security.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthTokenResponse response = authService.register(
                request,
                extractClientIp(httpServletRequest),
                httpServletRequest.getHeader("User-Agent")
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthTokenResponse response = authService.login(
                request,
                extractClientIp(httpServletRequest),
                httpServletRequest.getHeader("User-Agent")
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthTokenResponse response = authService.refresh(
                request,
                extractClientIp(httpServletRequest),
                httpServletRequest.getHeader("User-Agent")
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.successMessage("Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> me(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }

        CurrentUserResponse response = authService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
