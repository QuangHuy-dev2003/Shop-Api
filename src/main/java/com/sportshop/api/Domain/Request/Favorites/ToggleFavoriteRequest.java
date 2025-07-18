package com.sportshop.api.Domain.Request.Favorites;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToggleFavoriteRequest {
    private Long userId;
    private Long productId;
}