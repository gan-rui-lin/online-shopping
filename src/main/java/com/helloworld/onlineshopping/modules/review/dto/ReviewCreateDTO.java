package com.helloworld.onlineshopping.modules.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ReviewCreateDTO {
    @NotNull(message = "Order item ID is required")
    private Long orderItemId;

    @NotNull(message = "Score is required")
    @Min(1)
    @Max(5)
    private Integer score;

    private String content;
    private List<String> imageUrls;
    private Integer anonymousFlag = 0;
}
