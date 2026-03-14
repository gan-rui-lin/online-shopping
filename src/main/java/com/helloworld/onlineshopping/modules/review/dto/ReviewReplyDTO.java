package com.helloworld.onlineshopping.modules.review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewReplyDTO {
    @NotBlank(message = "Reply content cannot be empty")
    private String replyContent;
}
