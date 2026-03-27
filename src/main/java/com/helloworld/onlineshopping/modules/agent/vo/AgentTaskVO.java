package com.helloworld.onlineshopping.modules.agent.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentTaskVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;
    private String taskType;
    private Integer taskStatus;
    private String userPrompt;
    private LocalDateTime createTime;
    private List<AgentRecommendationVO> recommendations;
}
