package com.helloworld.onlineshopping.modules.plan.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.helloworld.onlineshopping.modules.plan.entity.ShoppingPlanEntity;
import com.helloworld.onlineshopping.modules.plan.mapper.ShoppingPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanReminderScheduler {
    private final ShoppingPlanMapper planMapper;

    @Scheduled(fixedRate = 60000)
    public void remind() {
        List<ShoppingPlanEntity> plans = planMapper.selectList(new LambdaQueryWrapper<ShoppingPlanEntity>()
            .eq(ShoppingPlanEntity::getPlanStatus, 0).le(ShoppingPlanEntity::getTriggerTime, LocalDateTime.now()));
        for (ShoppingPlanEntity plan : plans) {
            plan.setPlanStatus(1);
            planMapper.updateById(plan);
            log.info("Plan reminder: {}", plan.getPlanName());
        }
    }
}
