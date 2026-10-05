package com.his.medicaltech.trigger;

import com.his.medicaltech.service.CriticalValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 危急值超时升级的三条触发路径之一：<b>定时扫描</b>。
 *
 * <p>设计参照 {@code DayEndSettleTrigger} 的「定时 + 手工补跑」双路径：
 * 升级方法 {@code escalateOverdue()} 本身可重入（escalate_status 落库防重复），
 * 定时漏跑一轮不会丢，下一轮会补上；手工端点
 * {@code POST /medicaltech/criticalValue/escalateOverdue} 供验证与运维即时补跑。
 *
 * <p>扫描间隔 5 分钟的取值：危急值处置时限默认 30 分钟，超时 5 分钟内被发现
 * 足够及时；更密的扫描只是空转（绝大多数轮次影响 0 条）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CriticalValueEscalateTrigger {

    private final CriticalValueService criticalValueService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void scheduledEscalateOverdue() {
        try {
            criticalValueService.escalateOverdue();
        } catch (Exception e) {
            // 定时任务失败只记日志：升级是催办手段，不能影响任何业务线程
            log.error("[危急值] 超时升级定时任务失败：{}", e.getMessage(), e);
        }
    }
}
