package com.his.patient.trigger;

import com.his.patient.service.DeathCertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 死亡证明逾期未上报催报的触发器（death-cert 发送方）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeathCertOverdueTrigger {

    private final DeathCertificateService deathCertificateService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void scheduledNotifyOverdue() {
        try {
            deathCertificateService.notifyOverdue();
        } catch (Exception e) {
            // 催报是催办手段，失败只记日志，不能带崩任何业务线程
            log.error("[死亡证明] 逾期催报定时任务失败：{}", e.getMessage(), e);
        }
    }
}
