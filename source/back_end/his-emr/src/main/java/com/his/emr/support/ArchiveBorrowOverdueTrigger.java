package com.his.emr.support;

import com.his.emr.service.ArchiveBorrowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 病案借阅超期提醒的触发器（emr-arch 发送方）。
 *
 * <p>设计参照 {@link MedicalRecordArchiveOverdueTrigger} 的「定时 + 手工补跑」双路径：
 * {@code notifyOverdue()} 可重入（每张借阅单每天最多一条消息，靠消息通知当日已有记录去重），
 * 手工端点 {@code POST /charge/archiveBorrow/notifyOverdue} 供验证与运维即时补跑。
 *
 * <p>每天 08:30 扫一次：借阅超期以「天」为粒度（应还日期是 DATE），更密的扫描只是空转。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArchiveBorrowOverdueTrigger {

    private final ArchiveBorrowService borrowService;

    @Scheduled(cron = "0 30 8 * * ?")
    public void scheduledNotifyOverdue() {
        try {
            borrowService.notifyOverdue();
        } catch (Exception e) {
            // 定时任务失败只记日志：提醒是催办手段，不能影响任何业务线程
            log.error("[病案借阅] 超期提醒定时任务失败：{}", e.getMessage(), e);
        }
    }
}
