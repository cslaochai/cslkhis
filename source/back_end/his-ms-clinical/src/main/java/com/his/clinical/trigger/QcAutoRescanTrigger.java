package com.his.clinical.trigger;

import com.his.clinical.service.QualityControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 门诊自动质控补扫的触发器（质控域自取，不靠结诊方来回调）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QcAutoRescanTrigger {

    private static final int BATCH_LIMIT = 200;

    private final QualityControlService qualityControlService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void scheduledRescan() {
        try {
            qualityControlService.autoRescanOutpatientQc(BATCH_LIMIT);
        } catch (Exception e) {
            // 定时任务失败只记日志：补齐是兜底路径，不能拖住调度线程
            log.error("[质控补扫] 门诊自动质控补扫失败：{}", e.getMessage(), e);
        }
    }
}
