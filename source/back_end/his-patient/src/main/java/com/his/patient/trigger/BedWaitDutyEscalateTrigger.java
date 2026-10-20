package com.his.patient.trigger;

import com.his.patient.service.BedCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 等床超时 → 催当日总值班的定时触发器（duty-coord 的发送方之一，sql/169）。
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
