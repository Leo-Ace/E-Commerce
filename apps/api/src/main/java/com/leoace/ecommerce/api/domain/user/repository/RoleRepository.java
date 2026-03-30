package com.leoace.ecommerce.api.domain.user.repository;

import com.leoace.ecommerce.api.domain.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Role> findByCodeAndIsActiveTrue(String code);
}
