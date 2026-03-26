package com.helloworld.onlineshopping.modules.recommendation.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendEvent implements Serializable {
    private Long userId;
    private Long spuId;
    private RecommendEventType eventType;
    private LocalDateTime timestamp;

    /** Optional: review score (1-5), only set for REVIEW_SUBMIT */
    private Integer reviewScore;

    public static RecommendEvent of(Long userId, Long spuId, RecommendEventType type) {
        return new RecommendEvent(userId, spuId, type, LocalDateTime.now(), null);
    }

    public static RecommendEvent ofReview(Long userId, Long spuId, int score) {
        return new RecommendEvent(userId, spuId, RecommendEventType.REVIEW_SUBMIT, LocalDateTime.now(), score);
    }
}
