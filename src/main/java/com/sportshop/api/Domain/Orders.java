package com.sportshop.api.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "order_date", updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Column(name = "payment_amount", nullable = false)
    private Long total_amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_method", nullable = false, length = 50)
    private ShippingMethod shippingMethod;

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discounts discount;

    @Column(name = "discount_amount")
    private Long discountAmount;

    @Column(name = "shipping_fee")
    private Long shippingFee;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order_items> order_items;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Payments> payments;

    @Column(name = "order_code", unique = true, length = 20)
    private String orderCode;

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPING,
        DELIVERED,
        CANCELLED,
        RETURNED
    }

    public enum PaymentStatus {
        UNPAID,
        PAID,
        PENDING,
        REFUNDED
    }

    public enum PaymentMethod {
        CASH_ON_DELIVERY,
        VNPAY,
        MOMO,
        PAYPAL
    }

    public enum ShippingMethod {
        STANDARD,
        EXPRESS
    }
}
