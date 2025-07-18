package com.sportshop.api.Domain.Reponse.Favorites;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productCode;
    private BigDecimal price;
    private Integer sale;
    private String mainImage;
    private LocalDateTime addedAt;
}