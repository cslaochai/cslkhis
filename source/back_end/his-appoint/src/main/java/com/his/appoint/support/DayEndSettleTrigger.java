package com.his.appoint.support;

import com.his.appoint.service.DayEndSettleService;
import com.his.appoint.vo.DayEndSettleResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 日终结转的三条触发路径之一：<b>进页面顺手补跑</b>。
 *
 * <p>三条路径各管一段，缺一段就有漏：
 * <ol>
 *   <li>懒触发（本类）——谁打开门诊相关页面，谁顺手把遗留收掉。好处是<b>不需要运维记得点什么</b>；
 *       一天只跑一次（内存标记），失败不抛异常（结转失败不能让页面打不开）。</li>
 *   <li>夜里的定时任务（{@link #scheduledDayEndSettle()}）——当天门诊结束就收，第二天开盘时数据已是干净的；</li>
 *   <li>手工补跑（{@code /appoint/dayEndSettle/run}）——停诊、导数据、改库之后的重放。</li>
 * </ol>
 *
 * <p>为什么要三条：只做定时的话，进程没起来的那天就永远没人收（而 @Scheduled 不会补跑）；
 * 只做懒触发的话，没人开页面就没收（报表直接读库时看到的是中间态）；
 * 只做手工的话，一定会忘。
 *
 * <p>线程安全：用 {@code synchronized} + 内存日期标记，而不是分布式锁 ——
 * 结转本身<b>可重入</b>（只认中间态的行，跑两遍第二遍影响 0 条），并发跑最坏是白跑一次，
 * 不值得为它引入跨节点协调。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DayEndSettleTrigger {

    private final DayEndSettleService dayEndSettleService;

    /**
     * 上次成功触发的自然日；同一天不重复跑
     */
    private final AtomicReference<LocalDate> lastTriggered = new AtomicReference<>();

    /**
     * 进门诊相关页面时调用：把「昨天及更早」的遗留收掉。
     *
     * <p>刻意<b>不</b>往外抛异常：这是顺手的维护动作，失败只记日志。
     */
    public void ensureSettledUpToYesterday() {
        LocalDate today = LocalDate.now();
        LocalDate done = lastTriggered.get();
        if (done != null && !done.isBefore(today)) {
            return;
        }
        synchronized (this) {
            done = lastTriggered.get();
            if (done != null && !done.isBefore(today)) {
                return;
            }
            try {
                DayEndSettleResultVO r = dayEndSettleService.settlePending(false);
                if (r.getNoShowCount() + r.getUnvisitedCount() + r.getQueueExpiredCount() > 0) {
                    log.info("[日终结转] 懒触发补跑 {} ~ {}：{}", r.getFromDate(), r.getToDate(), r.getMessage());
                }
                lastTriggered.set(today);
            } catch (Exception e) {
                // 不置标记：下次进页面还能再试
                log.warn("[日终结转] 懒触发失败（不影响本次请求）：{}", e.getMessage());
            }
        }
    }

    /**
     * 每天 00:10 结转「刚结束的那一天」。
     *
     * <p>时间点的选择：<b>不能是当天 23:5x</b>。门诊结束时间没有硬边界（晚班、留观、加号），
     * 23:50 跑会把还在诊的患者判成「未就诊」。放到次日 00:10，被结转的那一天一定已经过完了，
     * 「不许结转今天」这条护栏也就自然满足。
     *
     * <p>跑的是 {@code settlePending} 而不是只跑昨天：进程停过几天时会一次性补齐中间每一天，
     * 单次上限 {@link DayEndSettleService#MAX_BACKFILL_DAYS} 天。
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void scheduledDayEndSettle() {
        lastTriggered.set(null);
        try {
            DayEndSettleResultVO r = dayEndSettleService.settlePending(false);
            log.info("[日终结转] 定时任务：{}", r.getMessage());
        } catch (Exception e) {
            log.error("[日终结转] 定时任务失败：{}", e.getMessage(), e);
        }
    }
}
