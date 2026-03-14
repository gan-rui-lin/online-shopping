package com.helloworld.onlineshopping.modules.favorite.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteToggleDTO {
    @NotNull(message = "SPU ID is required")
    private Long spuId;
}
