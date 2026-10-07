package com.his.appoint.trigger;

import com.his.appoint.service.BizEmergencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 急诊候诊超时升级的三条触发路径之一：<b>定时扫描</b>。
 *
 * <p>设计参照 {@code CriticalValueEscalateTrigger} 的「定时 + 手工补跑」双路径。
 * 与危急值不同的是这里<b>不加 escalate_status 列</b>：超时本身是
 * 入院时间与 NOW() 的函数，每轮重算即可；重复投递由
 * {@code receiverIdsOfBiz} 判重（一条急诊对同一收件人只催一次），
 * 所以 5 分钟一轮的扫描绝大多数轮次影响 0 条，不会把收件箱刷满。
 *
 * <p>扫描间隔 5 分钟：Ⅱ级危重的时限是 10 分钟，再密的扫描也只是空转。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencyWaitEscalateTrigger {

    private final BizEmergencyService bizEmergencyService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void scheduledEscalateOverdue() {
        try {
            bizEmergencyService.escalateOverdue();
        } catch (Exception e) {
            // 定时任务失败只记日志：催办不能影响任何业务线程
            log.error("[急诊候诊] 超时升级定时任务失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 留观超时限催办：与候诊升级同频同策略（判重靠 {@code receiverIdsOfBiz}，每人只催一次），
     * 所以放在同一个触发器里而不是另建一份 cron —— 两件事都是"急诊科该有人被追问"，
     * 分开排班只会让排查"为什么没报警"时多一处要看的代码。
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void scheduledEscalateObservation() {
        try {
            bizEmergencyService.escalateObservation();
        } catch (Exception e) {
            log.error("[急诊留观] 超时限催办定时任务失败：{}", e.getMessage(), e);
        }
    }
}
