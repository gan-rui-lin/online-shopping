package com.helloworld.onlineshopping.modules.rag.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RagAskDTO {
    private Long spuId;
    private List<Long> spuIds;
    @NotBlank(message = "Question is required")
    private String question;
    private Long sessionId;
    private String locale;
}
