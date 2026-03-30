package com.leoace.ecommerce.api.domain.seller.repository;

import com.leoace.ecommerce.api.domain.seller.entity.SellerUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SellerUserRepository extends JpaRepository<SellerUser, UUID> {

    List<SellerUser> findByUserId(UUID userId);

    List<SellerUser> findBySellerId(UUID sellerId);

    boolean existsBySellerIdAndUserId(UUID sellerId, UUID userId);
}
