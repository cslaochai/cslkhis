package com.his.patient.support;

import com.his.patient.service.DeathCertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 死亡证明逾期未上报催报的触发器（death-cert 发送方）。
 *
 * <p>参照病案借阅超期的「定时 + 手工补跑」双路径：{@code notifyOverdue()} 按天幂等
 * （靠死亡医学证明书.notify_time 当日不重发），手工端点
 * {@code POST /patient/death/cert/notifyOverdue} 供验证与运维即时补跑。
 *
 * <p>每天 09:00 扫一次：上报时限以「天」为粒度（死亡时间 + 院内口径天数），扫得更密只是空转。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeathCertOverdueTrigger {

    private final DeathCertificateService deathCertificateService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void scheduledNotifyOverdue() {
        try {
            deathCertificateService.notifyOverdue();
        } catch (Exception e) {
            // 催报是催办手段，失败只记日志，不能带崩任何业务线程
            log.error("[死亡证明] 逾期催报定时任务失败：{}", e.getMessage(), e);
        }
    }
}
