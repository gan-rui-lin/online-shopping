package com.helloworld.onlineshopping.modules.rag.vo;
import lombok.Data;
import java.util.List;
@Data
public class RagAnswerVO {
    private String question;
    private String answer;
    private Long sessionId;
    private List<String> referenceDocTitles;
}
