package com.his.outpatient.trigger;

import com.his.outpatient.service.DutyCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 听班呼叫超时升级的定时触发路径（sql/297）。
 *
 * <p>每分钟扫一轮：到岗时限内没人应答的呼叫单 → 升级到上一层级。
 * 「是否升级过」落库（escalate_status），所以重跑是幂等的 —— 补跑不会二次轰炸。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DutyCallEscalateTrigger {

    private final DutyCallService dutyCallService;

    @Scheduled(cron = "0 * * * * ?")
    public void scheduledEscalateOverdue() {
        try {
            dutyCallService.escalateOverdue();
        } catch (Exception e) {
            // 定时任务失败只记日志：催办/升级不能带崩任何业务线程
            log.error("[听班呼叫] 超时升级定时任务失败：{}", e.getMessage(), e);
        }
    }
}
