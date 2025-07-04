package com.sportshop.api.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product_variants {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false, length = 10)
    private Size size;

    @Column(name = "color", nullable = false, length = 10)
    private String color;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Cart_item> cart_items;

    public enum Size {
        // size cho áo
        XS, S, M, L, XL, XXL, XXXL,
        // Size số cho giày
        SIZE_36, SIZE_37, SIZE_38, SIZE_39, SIZE_40, SIZE_41, SIZE_42, SIZE_43, SIZE_44, SIZE_45, SIZE_46,
        // Size số cho quần áo
        WAIST_28, WAIST_29, WAIST_30, WAIST_31, WAIST_32, WAIST_33, WAIST_34, WAIST_35, WAIST_36,
        WAIST_38, WAIST_40, WAIST_42, WAIST_44, WAIST_46, WAIST_48, WAIST_50
    }
}
