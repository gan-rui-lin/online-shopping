package com.helloworld.onlineshopping.modules.ai.vo;

import lombok.Data;

import java.util.List;

@Data
public class CopywritingResultVO {
    private String content;
    private List<String> variants;
}
