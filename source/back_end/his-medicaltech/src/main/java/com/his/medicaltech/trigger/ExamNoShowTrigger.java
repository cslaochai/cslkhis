package com.his.medicaltech.trigger;

import com.his.medicaltech.service.ExamAppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 检查预约爽约扫描的双路径之一：<b>定时扫描</b>（另一条是手工端点
 * {@code POST /medicaltech/examAppoint/autoNoShow}，供验证与运维即时补跑）。
 *
 * <p>每 10 分钟一轮：号源的价值在于"过点没来就该还给别人"，10 分钟已经足够及时；
 * 判定条件写在 SQL 里（停在「已预约」且时段已过），可重入 —— 处理过的单状态变成 5，
 * 下一轮扫不到，因此定时漏跑一轮不会重复退号，也不会漏掉。
 *
 * <p>失败只记日志：扫描是回收手段，不能让它的异常影响任何业务线程或应用启动。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamNoShowTrigger {

    private final ExamAppointmentService appointmentService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void scheduledNoShow() {
        try {
            int n = appointmentService.autoNoShow();
            if (n > 0) {
                log.info("[检查预约] 定时扫描判定爽约 {} 张", n);
            }
        } catch (Exception e) {
            log.error("[检查预约] 爽约定时扫描失败：{}", e.getMessage(), e);
        }
    }
}
