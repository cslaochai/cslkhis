package com.his.clinical.trigger;

import com.his.clinical.service.QualityControlService;
import com.his.common.support.SystemOperatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 自动质控补扫触发器（质控域自取，不靠结诊方来回调）。
 *
 * <p>门诊侧兜的是「结诊收尾丢失」，住院侧兜的是「提交后没人跑引擎」——
 * 住院文书原先是纯人工质控，这里补上之后，提交过的住院文书会在 10 分钟内被自动跑一遍。
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
            SystemOperatorContext.bindSystemTask();
            qualityControlService.autoRescanOutpatientQc(BATCH_LIMIT);
            qualityControlService.autoRescanInpatientQc(BATCH_LIMIT);
        } catch (Exception e) {
            // 定时任务失败只记日志：补齐是兜底路径，不能拖住调度线程
            log.error("[质控补扫] 自动质控补扫失败：{}", e.getMessage(), e);
        } finally {
            SystemOperatorContext.clear();
        }
    }
}
