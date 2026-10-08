package com.his.emr.trigger;

import com.his.emr.service.ArchiveBorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 病案借阅超期提醒的触发器（emr-arch 发送方）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArchiveBorrowOverdueTrigger {

    private final ArchiveBorrowService archiveBorrowService;

    @Scheduled(cron = "0 30 8 * * ?")
    public void scheduledNotifyOverdue() {
        try {
            archiveBorrowService.notifyOverdue();
        } catch (Exception e) {
            // 定时任务失败只记日志：提醒是催办手段，不能影响任何业务线程
            log.error("[病案借阅] 超期提醒定时任务失败：{}", e.getMessage(), e);
        }
    }
}
