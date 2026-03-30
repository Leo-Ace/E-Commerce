package com.leoace.ecommerce.api.domain.seller.repository;

import com.leoace.ecommerce.api.domain.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SellerRepository extends JpaRepository<Seller, UUID> {

    Optional<Seller> findByCode(String code);

    Optional<Seller> findBySlug(String slug);

    boolean existsByCode(String code);

    boolean existsBySlug(String slug);
}
