package com.his.inpatient.trigger;

import com.his.common.support.SystemOperatorContext;
import com.his.inpatient.service.InpatientRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 住院文书草稿超时催办触发器（inpat-draft-overdue 发送方）。
 *
 * <p>规范时限：入院记录 24h、首次病程 8h、手术记录术后 24h、出院记录出院后 24h、转入记录转入后 24h，
 * 时限无节假日顺延。与门诊草稿催办同口径：24h 判重，提交后待办自然闭环。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InpatDraftOverdueTrigger {

    private static final int BATCH_LIMIT = 200;

    private final InpatientRecordService inpatientRecordService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void scheduledRemindOverdueDrafts() {
        try {
            SystemOperatorContext.bindSystemTask();
            inpatientRecordService.remindOverdueDrafts(BATCH_LIMIT);
        } catch (Exception e) {
            // 催办是兜底手段，失败只记日志，不能带崩调度线程
            log.error("[住院文书催办] 超时草稿扫描定时任务失败：{}", e.getMessage(), e);
        } finally {
            SystemOperatorContext.clear();
        }
    }
}
