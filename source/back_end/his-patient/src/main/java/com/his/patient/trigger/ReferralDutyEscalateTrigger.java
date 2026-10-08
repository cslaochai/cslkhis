package com.his.patient.trigger;

import com.his.patient.service.ReferralService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 双向转诊「待确认」超时 → 催当日总值班的定时触发器（duty-coord 的发送方之一，sql/169）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReferralDutyEscalateTrigger {

    private final ReferralService referralService;

    @Scheduled(cron = "0 */30 * * * ?")
    public void scheduledEscalatePendingToDuty() {
        try {
            referralService.escalatePendingToDuty();
        } catch (Exception e) {
            // 催办不能带崩任何业务线程
            log.error("[双向转诊] 定时催办任务失败：{}", e.getMessage(), e);
        }
    }
}
