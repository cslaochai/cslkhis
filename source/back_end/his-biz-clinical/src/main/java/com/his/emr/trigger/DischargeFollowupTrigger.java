package com.his.emr.trigger;

import com.his.emr.service.FollowupTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 出院自动建随访的触发器（随访域自取，不靠出院方来调）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DischargeFollowupTrigger {

    private static final int BATCH_LIMIT = 200;

    private final FollowupTaskService followupTaskService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void scheduledAutoCreate() {
        try {
            followupTaskService.autoCreateFromDischarge(BATCH_LIMIT);
        } catch (Exception e) {
            // 定时任务失败只记日志：补齐是兜底路径，不能拖住调度线程
            log.error("[出院随访] 自动补建随访计划失败：{}", e.getMessage(), e);
        }
    }
}
