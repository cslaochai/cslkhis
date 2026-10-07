package com.his.emr.trigger;

import com.his.emr.service.MedicalRecordArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 病历归档超期提醒的触发器（emr-arch 发送方）。
 *
 * <p>设计参照 {@code DayEndSettleTrigger} / {@code CriticalValueEscalateTrigger} 的
 * 「定时 + 手工补跑」双路径：{@code notifyOverdueArchives()} 可重入
 * （每份病历每天最多一条消息，靠消息通知当日已有记录去重），
 * 定时漏跑一轮不会丢，手工端点 {@code POST /charge/archive/notifyOverdue}
 * 供验证与运维即时补跑。
 *
 * <p>每天 08:00 扫一次：归档超期以「天」为粒度，更密的扫描只是空转。
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
