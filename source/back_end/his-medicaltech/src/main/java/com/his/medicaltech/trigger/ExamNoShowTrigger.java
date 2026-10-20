package com.his.medicaltech.trigger;

import com.his.medicaltech.service.ExamAppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 检查预约爽约扫描的双路径之一：定时扫描（另一条是手工端点
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamNoShowTrigger {

    private final ExamAppointmentService examAppointmentService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void scheduledNoShow() {
        try {
            int n = examAppointmentService.autoNoShow();
            if (n > 0) {
                log.info("[检查预约] 定时扫描判定爽约 {} 张", n);
            }
        } catch (Exception e) {
            log.error("[检查预约] 爽约定时扫描失败：{}", e.getMessage(), e);
        }
    }
}
