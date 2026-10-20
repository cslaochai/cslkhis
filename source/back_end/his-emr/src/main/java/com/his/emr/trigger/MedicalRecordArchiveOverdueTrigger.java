package com.his.emr.trigger;

import com.his.emr.service.MedicalRecordArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 病历归档超期提醒的触发器（emr-arch 发送方）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MedicalRecordArchiveOverdueTrigger {

    private final MedicalRecordArchiveService medicalRecordArchiveService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduledNotifyOverdue() {
        try {
            medicalRecordArchiveService.notifyOverdueArchives();
        } catch (Exception e) {
            // 定时任务失败只记日志：提醒是催办手段，不能影响任何业务线程
            log.error("[病历归档] 归档超期提醒定时任务失败：{}", e.getMessage(), e);
        }
    }
}
