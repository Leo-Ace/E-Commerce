package com.leoace.ecommerce.api.domain.seller.entity;

import com.leoace.ecommerce.api.common.entity.BaseAuditEntity;
import com.leoace.ecommerce.api.domain.seller.enums.SellerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
public class Seller extends BaseAuditEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "phone", unique = true, length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SellerStatus status = SellerStatus.PENDING;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
