package com.helloworld.onlineshopping.modules.agent.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentTaskVO {
    private Long taskId;
    private String taskType;
    private Integer taskStatus;
    private String userPrompt;
    private LocalDateTime createTime;
    private List<AgentRecommendationVO> recommendations;
}
