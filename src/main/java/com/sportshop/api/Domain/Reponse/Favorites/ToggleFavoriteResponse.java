package com.sportshop.api.Domain.Reponse.Favorites;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToggleFavoriteResponse {
    private boolean isFavorite;
    private String message;
}