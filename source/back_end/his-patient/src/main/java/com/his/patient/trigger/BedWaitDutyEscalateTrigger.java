package com.his.patient.trigger;

import com.his.patient.service.BedCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 等床超时 → 催当日总值班的定时触发器（duty-coord 的发送方之一，sql/169）。
 *
 * <p>与 {@code EmergencyWaitEscalateTrigger} 同思路：<b>定时 + 手工补跑</b>，
 * 手工端点在 {@code /patient/inpatient/bedCenter/escalateWaitToDuty}（验证与运维即时跑一轮）。
 *
 * <p>每小时整点扫一次：阈值是 24 小时级别的等待，扫得更密只是空转；
 * 判重靠 {@code receiverIdsOfBiz}（同一条等床记录对同一总值班只催一次），
 * 所以绝大多数轮次影响 0 条，不会把收件箱刷满。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BedWaitDutyEscalateTrigger {

    private final BedCenterService bedCenterService;

    @Scheduled(cron = "0 0 * * * ?")
    public void scheduledEscalateWaitToDuty() {
        try {
            bedCenterService.escalateWaitToDuty();
        } catch (Exception e) {
            // 催办不能带崩任何业务线程
            log.error("[等床协调] 定时催办任务失败：{}", e.getMessage(), e);
        }
    }
}
