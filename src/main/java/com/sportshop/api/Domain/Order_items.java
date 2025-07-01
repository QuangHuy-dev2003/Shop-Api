package com.sportshop.api.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order_items {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private Long unit_price;

    @ManyToOne
    @JoinColumn(name = "variant_id")
    private Product_variants variant;

    @Column(name = "subtotal")
    private Long subtotal;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "size", length = 10)
    private String size;

    @Column(name = "color", length = 50)
    private String color;

}
