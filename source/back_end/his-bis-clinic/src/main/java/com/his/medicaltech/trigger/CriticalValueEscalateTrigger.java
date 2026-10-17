package com.his.medicaltech.trigger;

import com.his.medicaltech.service.CriticalValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 危急值超时升级的三条触发路径之一：定时扫描。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriticalValueEscalateTrigger {

    private final CriticalValueService criticalValueService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void scheduledEscalateOverdue() {
        try {
            criticalValueService.escalateOverdue();
        } catch (Exception e) {
            // 定时任务失败只记日志：升级是催办手段，不能影响任何业务线程
            log.error("[危急值] 超时升级定时任务失败：{}", e.getMessage(), e);
        }
    }
}
