package com.his.patient.support;

import com.his.patient.service.ReferralService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 双向转诊「待确认」超时 → 催当日总值班的定时触发器（duty-coord 的发送方之一，sql/169）。
 *
 * <p>每 30 分钟扫一次：上转是急事（联系上级医院 + 安排转运），阈值默认 2 小时，
 * 扫得太稀会让"刚过阈值的急单"拖到下一轮；判重按时间窗（同阈值），所以重复轮次影响 0 条。
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
