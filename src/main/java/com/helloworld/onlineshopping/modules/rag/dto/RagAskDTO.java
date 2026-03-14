package com.helloworld.onlineshopping.modules.rag.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class RagAskDTO {
    @NotNull(message = "SPU ID is required")
    private Long spuId;
    @NotBlank(message = "Question is required")
    private String question;
    private Long sessionId;
}
