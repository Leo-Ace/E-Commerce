package com.leoace.ecommerce.api.domain.seller.entity;

import com.leoace.ecommerce.api.common.entity.BaseAuditEntity;
import com.leoace.ecommerce.api.domain.seller.enums.SellerMembershipRole;
import com.leoace.ecommerce.api.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "seller_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seller_users_seller_user", columnNames = {"seller_id", "user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class SellerUser extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_role", nullable = false, length = 30)
    private SellerMembershipRole membershipRole = SellerMembershipRole.STAFF;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
