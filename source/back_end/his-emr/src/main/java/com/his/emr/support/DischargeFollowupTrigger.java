package com.his.emr.support;

import com.his.emr.service.FollowupTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 出院自动建随访的触发器（随访域自取，不靠出院方来调）。
 *
 * <p><b>为什么是补齐而不是同步建单</b>：出院办理住在 his-patient，随访任务写在本模块，
 * 依赖方向只有 随访→出院 一条；让出院那侧直接调过来会成环，Maven 直接构建失败。
 * 所以把「存活出院必须有一条随访计划」这条规则的兑现动作放回规则的归属方。
 *
 * <p>每 10 分钟一次，单次最多 200 条：随访计划到期日在出院后 7 天，十分钟的延迟业务上无感；
 * 建单带幂等锚，重复扫描不会重复建，漏一轮下一轮还会补上。
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
