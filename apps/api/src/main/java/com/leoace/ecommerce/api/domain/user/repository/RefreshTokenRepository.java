package com.leoace.ecommerce.api.domain.user.repository;

import com.leoace.ecommerce.api.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @EntityGraph(attributePaths = {"user", "user.roles", "user.roles.permissions"})
    Optional<RefreshToken> findWithUserByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(UUID userId);
}