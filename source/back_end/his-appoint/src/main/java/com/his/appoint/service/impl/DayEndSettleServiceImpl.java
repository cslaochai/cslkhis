package com.his.appoint.service.impl;

import com.his.appoint.dto.DayEndSettleDTO;
import com.his.appoint.mapper.DayEndSettleMapper;
import com.his.appoint.service.DayEndSettleService;
import com.his.appoint.vo.DayEndSettleResultVO;
import com.his.common.exception.BusinessException;
import com.his.security.entity.CurrentUser;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 日终结转实现。
 *
 * <p>口径与取舍见 {@link DayEndSettleService} 接口注释；这里只说两处容易改错的细节：
 *
 * <p>1）<b>操作人取登录态</b>，不信前端。定时任务没有登录态，落 {@code system:dayEndSettle}。
 * 这样「谁把这批号收掉的」在库里查得到。
 *
 * <p>2）<b>不许结转今天</b>。今天的门诊可能还在进行（还有患者候诊、还有医生在看），
 * 把今天收成「未就诊」等于把在诊患者判死。要碰今天，只能等它变成昨天。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DayEndSettleServiceImpl implements DayEndSettleService {

    private static final String SYSTEM_OPERATOR = "system:dayEndSettle";

    private final DayEndSettleMapper dayEndSettleMapper;

    /**
     * 留痕文案；重复跑不会叠加（UPDATE 里判断过 remark 已含「日终结转」就原样保留）
     */
    private static String marker(LocalDate d) {
        return "【日终结转 " + d + " 自动收尾】";
    }

    private static String currentOperator() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            return SYSTEM_OPERATOR;
        }
        if (user.getEmployeeId() != null) {
            return user.getEmployeeName() == null
                    ? String.valueOf(user.getEmployeeId())
                    : user.getEmployeeName() + "(" + user.getEmployeeId() + ")";
        }
        return user.getUsername() == null ? SYSTEM_OPERATOR : user.getUsername();
    }

    private static DayEndSettleResultVO build(LocalDate from, LocalDate to, long noShow, long unvisited,
                                              long queueExpired, boolean dryRun, String message, boolean emptyRange) {
        DayEndSettleResultVO vo = new DayEndSettleResultVO();
        vo.setFromDate(from);
        vo.setToDate(to);
        vo.setDays(emptyRange || from == null || to == null ? 0 : (int) ChronoUnit.DAYS.between(from, to) + 1);
        vo.setNoShowCount(noShow);
        vo.setUnvisitedCount(unvisited);
        vo.setQueueExpiredCount(queueExpired);
        vo.setDryRun(dryRun);
        vo.setMessage(message);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DayEndSettleResultVO settle(DayEndSettleDTO dto) {
        boolean dryRun = dto != null && Boolean.TRUE.equals(dto.getDryRun());
        if (dto != null && dto.getSettleDate() != null) {
            // 指定日期 = 只结转这一天（页面选了某天时用）
            LocalDate target = dto.getSettleDate();
            return doSettle(target, target, dryRun);
        }
        // 不指定日期 = 补跑「从最早遗留日到昨天」。
        //
        // 这里原本写的是「只结转昨天」，与 DTO 上的 @Schema 说明相反 —— 结果是页面上看得见
        // 6 条爽约、却怎么也清不掉更早那几天的遗留（手工按钮和懒触发的口径不一样，用户没法理解）。
        // 手工按钮要的正是「把积压一次清干净」，所以直接复用懒触发那条路径，一天只有一个口径。
        return settlePending(dryRun);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DayEndSettleResultVO settlePending(boolean dryRun) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate earliest = dayEndSettleMapper.selectEarliestUnsettledDate();
        if (earliest == null) {
            return build(earliest, yesterday, 0, 0, 0, dryRun, "没有需要收尾的遗留记录", true);
        }
        // 安全阀：空库/脏数据（例如 1970 年的行）不能把一次请求拖成几万天的循环
        LocalDate limit = yesterday.minusDays(MAX_BACKFILL_DAYS - 1L);
        LocalDate from = earliest.isBefore(limit) ? limit : earliest;
        return doSettle(from, yesterday, dryRun);
    }

    private DayEndSettleResultVO doSettle(LocalDate from, LocalDate to, boolean dryRun) {
        LocalDate today = LocalDate.now();
        if (to.isAfter(today.minusDays(1))) {
            throw new BusinessException("日终结转只能结转已经过去的就诊日（不能结转今天，今天的门诊可能还在进行）");
        }
        if (from.isAfter(to)) {
            return build(from, to, 0, 0, 0, dryRun, "结转区间为空", true);
        }
        String operator = currentOperator();
        long noShow = 0;
        long unvisited = 0;
        long queueExpired = 0;
        long queueAligned = 0;
        long stuck = 0;
        int days = (int) ChronoUnit.DAYS.between(from, to) + 1;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            noShow += dayEndSettleMapper.countNoShow(d);
            unvisited += dayEndSettleMapper.countUnvisited(d);
            queueExpired += dayEndSettleMapper.countOpenQueue(d);
            queueAligned += dayEndSettleMapper.countQueueRegistMismatch(d);
            stuck += dayEndSettleMapper.countStuckConsulting(d);
            if (!dryRun) {
                // 顺序不能换：③ 判「名下有队列行」要用到还在 2/3 的队列行，所以 ④ 必须最后；
                // ① 先做是为了让「退号了队列还挂着候诊」的行不再被 ③ 当成「到过院没走完」。
                dayEndSettleMapper.alignQueueToRegistTerminal(d, operator);
                dayEndSettleMapper.markNoShow(d, marker(d), operator);
                dayEndSettleMapper.markUnvisited(d, marker(d), operator);
                dayEndSettleMapper.expireQueue(d, operator);
            }
        }
        if (!dryRun && (noShow > 0 || unvisited > 0 || queueExpired > 0 || queueAligned > 0)) {
            log.info("[日终结转] {} ~ {}：爽约 {} 未就诊 {}（其中已接诊未结诊 {}）队列失效 {} 队列对齐 {}（操作人 {}）",
                    from, to, noShow, unvisited, stuck, queueExpired, queueAligned, operator);
        }
        String msg = dryRun
                ? String.format("试算：爽约 %d、未就诊 %d（含已接诊未结诊 %d）、队列失效 %d、队列对齐 %d（未落库）",
                noShow, unvisited, stuck, queueExpired, queueAligned)
                : String.format("结转完成：爽约 %d、未就诊 %d（含已接诊未结诊 %d）、队列失效 %d、队列对齐 %d",
                noShow, unvisited, stuck, queueExpired, queueAligned);
        DayEndSettleResultVO vo = build(from, to, noShow, unvisited, queueExpired, dryRun, msg, false);
        vo.setQueueAlignedCount(queueAligned);
        vo.setStuckConsultingCount(stuck);
        return vo;
    }
}
