package com.helloworld.onlineshopping.modules.ai.dto;

import lombok.Data;

@Data
public class CopywritingRequestDTO {
    private String keywords;
    private String targetAudience;
    private String style;
    private String locale;
}
