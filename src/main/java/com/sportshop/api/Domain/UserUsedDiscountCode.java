package com.sportshop.api.Domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_used_discount_code")
@Data
public class UserUsedDiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discounts discount;

    @Column(name = "used_count")
    private Integer usedCount = 0;
}