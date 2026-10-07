package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.charge.dto.*;
import com.his.charge.entity.BizCashierSettlement;
import com.his.charge.entity.BizDaySettlement;
import com.his.charge.mapper.BizCashierSettlementMapper;
import com.his.charge.mapper.BizDaySettlementMapper;
import com.his.charge.service.FinanceSettlementService;
import com.his.charge.service.PaymentService;
import com.his.charge.vo.*;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 财务班结 / 日结 / 三级对账实现。
 *
 * <p>四层后本类的定位是<b>只读聚合 + 定格凭证</b>：一分钱都不在这里产生，
 * 全部事实来自 L3 支付资金流水（钱）与 L2 结算账单，
 * 本层只负责"把某个时刻看到的数钉死成凭证"并"拿三条链互相对"。
 *
 * <p>三级对账不是三次相同的检查，每一级都有**不同的判别力**（左右必须来自不同的表或不同取数路径，
 * 自己等于自己的断言不叫对账）：
 * <ul>
 *   <li><b>一级班结 ↔ 该班认领的流水（逐张复算）</b>：按交班单上的归集指针回 L3 现算一遍。
 *       交班后有流水被冲正、或班结单被直接改过，都会在这里露出来。</li>
 *   <li><b>二级 Σ班结 ↔ 全院流水实收</b>：班结是"有人认领的钱"，全院流水是"实际进的钱"，
 *       差额单列成 {@code unassignedCount/Amount} —— 忘交班的、交完班又收钱的、
 *       患者端自助缴费没人交班的，全在这一级显形。</li>
 *   <li><b>三级 Σ摊行（含无科室归属）↔ Σ账单单头</b>：同一批当日收讫账单，
 *       明细摊行合计与单头应收合计互校，抓的是"摊行漏写/多写"这类 L2 内部不一致。</li>
 *   <li><b>科室归集的完整性</b>由 {@code unattributedCount/Amount} 单列保证：无归属的钱
 *       绝不并进科室统计，新数据（记账时就带科室）应当 100% 有归属。</li>
 * </ul>
 *
 * <p><b>统筹不进现金侧的任何一环</b>：收费员班结单根本没有可用列，
 * 日结单的 {@code poolAmount} 只从当日<b>收讫账单</b>聚合，且不参与 {@code netAmount} 与各级差额
 * —— 它是医保局后付的钱，把它掺进"收银台收到的钱"里，两边就再也对不上了。
 */
@Service
@RequiredArgsConstructor
public class FinanceSettlementServiceImpl implements FinanceSettlementService {

    /**
     * 支付方式（{@code PaymentMethodEnum}）：1-现金 2-微信 3-支付宝 4-医保个账 5-院内余额 6-银行卡 7-转账。
     *
     * <p>6/7 没有对应的班结/日结列，落进 {@code unknownPayAmount}（见 {@code switch} 的 default 分支）——
     * 它们是真实渠道，但不是本表要单列的清点项，与其并进微信假装有数，不如显式承认"这一坨没有列"。
     */
    private static final int PAY_CASH = 1;
    private static final int PAY_WECHAT = 2;
    private static final int PAY_ALIPAY = 3;
    private static final int PAY_INSURANCE = 4;
    private static final int PAY_BALANCE = 5;

    private static final int SCALE = 2;

    private final BizCashierSettlementMapper cashierMapper;
    private final BizDaySettlementMapper dayMapper;
    /**
     * 只调它一个归集动作（{@code claimForShift}），聚合一律走本层自己的 Mapper 现算 ——
     * 日结不能靠"问 L3 要一个合计"，那样三条链就塌成一条，对账等于自查。
     */
    private final PaymentService paymentService;

    // 班结（收费员交班）

    private static CashierSettlementVO toCashierVO(BizCashierSettlement row) {
        if (row == null) {
            return null;
        }
        CashierSettlementVO vo = new CashierSettlementVO();
        BeanUtils.copyProperties(row, vo);
        return vo;
    }

    private static List<CashierSettlementVO> toCashierVOList(List<BizCashierSettlement> rows) {
        List<CashierSettlementVO> vos = new ArrayList<>(rows.size());
        for (BizCashierSettlement row : rows) {
            vos.add(toCashierVO(row));
        }
        return vos;
    }

    private static DaySettlementVO toDayVO(BizDaySettlement row) {
        if (row == null) {
            return null;
        }
        DaySettlementVO vo = new DaySettlementVO();
        BeanUtils.copyProperties(row, vo);
        return vo;
    }

    private static List<DaySettlementVO> toDayVOList(List<BizDaySettlement> rows) {
        List<DaySettlementVO> vos = new ArrayList<>(rows.size());
        for (BizDaySettlement row : rows) {
            vos.add(toDayVO(row));
        }
        return vos;
    }

    // 日结

    private static int pageNum(Integer v) {
        return v == null || v < 1 ? 1 : v;
    }

    private static int pageSize(Integer v) {
        return v == null || v < 1 || v > 200 ? 10 : v;
    }

    private static LocalDate parseDate(String s) {
        // C 类保留：日期解析工具被多个入口共用（@RequestParam 与非 web 调用），空值兜底留在原地，注解挂不到私有方法上
        if (!StringUtils.hasText(s)) {
            throw new BusinessException("日期不能为空");
        }
        try {
            return LocalDate.parse(s.trim().substring(0, 10), DateFormats.DATE);
        } catch (Exception e) {
            throw new BusinessException("日期格式应为 yyyy-MM-dd：" + s);
        }
    }

    private static LocalDateTime startOf(String date) {
        return StringUtils.hasText(date) ? parseDate(date).atStartOfDay() : null;
    }

    /**
     * 日期可空版：空返回 {@code null}，**不抛异常**。
     *
     * <p>给"条件式查询"用：{@code wrapper.ge(hasText(x), col, parseDateOrNull(x))} 里那个值参数
     * 无论条件真假都会被求值，用会抛异常的 parseDate 会让"不传日期"变成 500。
     */
    private static LocalDate parseDateOrNull(String date) {
        return StringUtils.hasText(date) ? parseDate(date) : null;
    }

    private static LocalDateTime endOf(String date) {
        return StringUtils.hasText(date) ? parseDate(date).atTime(23, 59, 59) : null;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    // 三级对账

    private static BigDecimal scale(BigDecimal v) {
        return nz(v).setScale(SCALE, RoundingMode.HALF_UP);
    }

    // 上下文构建

    /**
     * {@code COUNT(*)} 列在 MySQL 里是 BIGINT，MyBatis 取回来是 {@link Long}，
     * 而落库列与上下文口径都是 {@code int} —— 这里统一收口一次（null 当 0）。
     *
     * <p>所有笔数都有 {@code COALESCE} 或 {@code COUNT} 兜底，实际不会为 null；
     * 保留 null 判断是因为这里<b>不做静默截断</b>：真出现空值时应当当 0 记账，
     * 而不是抛 NPE 让整个日结跑不完（钱没算错，但一整天结不出来）。
     */
    private static int cntOf(Long v) {
        return v == null ? 0 : v.intValue();
    }

    private static BigDecimal ratio(BigDecimal part, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(new BigDecimal("100")).divide(total, SCALE, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CashierSettlementVO handover(CashierHandoverDTO dto) {
        Long cashierId = UserUtils.getCurrentUser().getEmployeeId();
        if (cashierId == null) {
            throw new BusinessException("无法识别当前收费员工号，交班失败");
        }
        String cashierName = UserUtils.getCurrentUser().getRealName();
        if (!StringUtils.hasText(cashierName)) {
            cashierName = "未知收费员";
        }

        LocalDateTime periodEnd = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        // 滚动区间：起点 = 上次交班时刻。首次交班回落到当日 00:00:00。
        // ⚠ 它只是凭证上的时间说明与"不足 1 秒"闸门的依据，**不决定本班的行集**
        //   （行集由下面的归集指针定义，见 claimForShift 的注释）。
        LocalDateTime lastEnd = cashierMapper.selectLastPeriodEnd(cashierId);
        LocalDateTime periodBegin = lastEnd != null ? lastEnd : LocalDate.now().atStartOfDay();
        if (!periodEnd.isAfter(periodBegin)) {
            throw new BusinessException("距上次交班（" + periodBegin + "）不足 1 秒，无需重复交班");
        }

        // 先立单再归集
        // 没有交班单 ID 就写不了指针，而金额必须在指针落定之后现算，所以这里插一条
        // 只有期头的空单；下面的差异闸门一旦抛错整事务回滚，空单和指针一起消失，不留半态。
        BizCashierSettlement entity = new BizCashierSettlement();
        entity.setSettlementNo(nextCashierNo());
        entity.setCashierId(cashierId);
        entity.setCashierName(cashierName);
        entity.setShiftType(dto.getShiftType());
        entity.setPeriodBegin(periodBegin);
        entity.setPeriodEnd(periodEnd);
        entity.setSettleStatus(1);
        entity.setRemark(dto.getRemark());
        cashierMapper.insert(entity);

        // 归集指针：把"这个班认领了哪些流水"登记下来。金额不参与统计，
        // 它只服务两件事 —— 班结/一级复算按同一集合取数，日结一眼看出还有哪些钱没人交班
        // （cashier_settlement_id IS NULL）。放在同一事务里：宁可交班整个失败回滚，
        // 也不能留下"班结了、流水还挂着未认领"的半态。
        paymentService.claimForShift(cashierId, periodEnd, entity.getId());

        // 系统账：本班认领的收款流水，按支付方式分桶
        List<PaymentMethodSumVO> payRows = cashierMapper.sumPaidBySettlement(entity.getId());
        PayBuckets buckets = new PayBuckets();
        for (PaymentMethodSumVO row : payRows) {
            buckets.add(row.getPaymentMethod(), nz(row.getAmount()), cntOf(row.getCnt()));
        }
        int chargeCount = buckets.count;
        BigDecimal chargeAmount = buckets.amount;
        BigDecimal cash = buckets.cash;

        // 本班经手的退费（掏出去的钱）
        CountAmountVO refundRow = cashierMapper.sumRefundBySettlement(entity.getId());
        int refundCount = cntOf(refundRow.getCnt());
        BigDecimal refundAmount = nz(refundRow.getAmount());

        // 票据
        InvoiceCountVO invoiceRow = cashierMapper.sumInvoiceBySettlement(entity.getId());
        int invoiceCount = cntOf(invoiceRow.getCnt());
        int invoiceVoidCount = cntOf(invoiceRow.getVoidCnt());

        // 现金差异
        BigDecimal handin = dto.getHandinCash().setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal cashDiff = handin.subtract(cash).setScale(SCALE, RoundingMode.HALF_UP);
        if (cashDiff.compareTo(BigDecimal.ZERO) != 0 && !StringUtils.hasText(dto.getDiffReason())) {
            throw new BusinessException("现金差异 " + cashDiff.toPlainString()
                    + " 元（实交 " + handin.toPlainString() + " / 系统 " + cash.toPlainString()
                    + "），必须填写差异说明");
        }

        entity.setChargeCount(chargeCount);
        entity.setChargeAmount(chargeAmount.setScale(SCALE, RoundingMode.HALF_UP));
        entity.setRefundCount(refundCount);
        entity.setRefundAmount(refundAmount.setScale(SCALE, RoundingMode.HALF_UP));
        entity.setNetAmount(entity.getChargeAmount().subtract(entity.getRefundAmount()));
        entity.setCashAmount(buckets.cash.setScale(SCALE, RoundingMode.HALF_UP));
        entity.setWechatAmount(buckets.wechat.setScale(SCALE, RoundingMode.HALF_UP));
        entity.setAlipayAmount(buckets.alipay.setScale(SCALE, RoundingMode.HALF_UP));
        entity.setInsuranceAmount(buckets.insurance.setScale(SCALE, RoundingMode.HALF_UP));
        entity.setBalanceAmount(buckets.balance.setScale(SCALE, RoundingMode.HALF_UP));
        entity.setUnknownPayAmount(buckets.unknownPay.setScale(SCALE, RoundingMode.HALF_UP));
        entity.setInvoiceCount(invoiceCount);
        entity.setInvoiceVoidCount(invoiceVoidCount);
        entity.setHandinCash(handin);
        entity.setCashDiff(cashDiff);
        entity.setDiffReason(dto.getDiffReason());
        cashierMapper.updateById(entity);
        return toCashierVO(entity);
    }

    @Override
    public PageResult<CashierSettlementVO> cashierPage(CashierSettlementQueryPageDTO dto) {
        LambdaQueryWrapper<BizCashierSettlement> w = new LambdaQueryWrapper<>();
        w.eq(dto.getCashierId() != null, BizCashierSettlement::getCashierId, dto.getCashierId())
                .eq(dto.getSettleStatus() != null, BizCashierSettlement::getSettleStatus, dto.getSettleStatus())
                .ge(StringUtils.hasText(dto.getDateStart()), BizCashierSettlement::getPeriodEnd, startOf(dto.getDateStart()))
                .le(StringUtils.hasText(dto.getDateEnd()), BizCashierSettlement::getPeriodEnd, endOf(dto.getDateEnd()))
                // 排序必须补唯一二级键：同一秒交班的两条（换班交接）顺序不稳定 → 翻页重复 + 丢行且不报错
                .orderByDesc(BizCashierSettlement::getPeriodEnd)
                .orderByDesc(BizCashierSettlement::getId);
        Page<BizCashierSettlement> page = cashierMapper.selectPage(
                new Page<>(pageNum(dto.getPageNum()), pageSize(dto.getPageSize())), w);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toCashierVOList(page.getRecords()));
    }

    @Override
    public CashierSettlementVO getCashierById(Long id) {
        // C 类保留：入参是 Long（GET 参数直传），没有 DTO 承载注解；@RequestParam 已 required，此处是直调兜底
        if (id == null) {
            throw new BusinessException("交班单ID不能为空");
        }
        BizCashierSettlement row = cashierMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("交班单不存在");
        }
        return toCashierVO(row);
    }

    // 辅助

    @Override
    public SettlementStatusCountVO statusCount() {
        SettlementStatusCountVO vo = new SettlementStatusCountVO();
        vo.setCashierPending(countCashier(1));
        vo.setCashierSettled(countCashier(2));
        vo.setCashierAudited(countCashier(3));
        vo.setDayPendingAudit(countDay(1));
        vo.setDayAudited(countDay(2));
        vo.setDayWithDiff(dayMapper.selectCount(new LambdaQueryWrapper<BizDaySettlement>()
                .eq(BizDaySettlement::getReconcileStatus, 2)));
        BigDecimal pendingAmount = BigDecimal.ZERO;
        for (BizCashierSettlement s : cashierMapper.selectList(new LambdaQueryWrapper<BizCashierSettlement>()
                .eq(BizCashierSettlement::getSettleStatus, 1))) {
            pendingAmount = pendingAmount.add(nz(s.getChargeAmount()));
        }
        vo.setCashierPendingAmount(pendingAmount.setScale(SCALE, RoundingMode.HALF_UP));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DaySettlementVO runDaySettlement(DaySettlementRunDTO dto) {
        LocalDate day = parseDate(dto.getSettleDate());

        BizDaySettlement existing = dayMapper.selectOne(new LambdaQueryWrapper<BizDaySettlement>()
                .eq(BizDaySettlement::getSettleDate, day)
                .last("LIMIT 1"));
        if (existing != null && Objects.equals(existing.getSettleStatus(), 2)) {
            throw new BusinessException("该日结单已审核，不可重算（如需更正请走冲正流程）");
        }

        DayContext ctx = buildContext(day);
        SettlementReconcileVO reconcile = reconcileOf(day, ctx);

        boolean allowDiff = Boolean.TRUE.equals(dto.getAllowDiff());
        if (!Boolean.TRUE.equals(reconcile.getPassed()) && !allowDiff) {
            // 差异必须先被人看见：把结论原样抛出来，而不是先落库再说
            throw new BusinessException("三级对账未平，无法日结：" + reconcile.getSummary()
                    + "（确认无误可勾选「放行差异」并填写理由）");
        }
        // B 类保留：条件必填——只有三级对账不平、勾选放行差异时才要求理由
        if (!Boolean.TRUE.equals(reconcile.getPassed()) && !StringUtils.hasText(dto.getRemark())) {
            throw new BusinessException("放行差异必须填写理由");
        }

        BizDaySettlement entity = existing != null ? existing : new BizDaySettlement();
        if (existing == null) {
            entity.setSettlementNo("RJ" + day.format(DateFormats.COMPACT_DATE));
        }
        entity.setSettleDate(day);
        entity.setShiftCount(ctx.shifts.size());
        // 资金链（L3 全院流水现算）—— 这是"今天到底进了多少钱"
        entity.setChargeCount(ctx.txChargeCount);
        entity.setChargeAmount(ctx.txChargeAmount);
        entity.setBillCount(ctx.billCount);
        entity.setRefundCount(ctx.txRefundCount);
        entity.setRefundAmount(ctx.txRefundAmount);
        entity.setNetAmount(ctx.txChargeAmount.subtract(ctx.txRefundAmount));
        entity.setCashAmount(scale(ctx.buckets.cash));
        entity.setWechatAmount(scale(ctx.buckets.wechat));
        entity.setAlipayAmount(scale(ctx.buckets.alipay));
        entity.setInsuranceAmount(scale(ctx.buckets.insurance));
        entity.setBalanceAmount(scale(ctx.buckets.balance));
        entity.setUnknownPayAmount(scale(ctx.buckets.unknownPay));
        // 统筹是医保局后付的钱，只在这里出现一次；不进 netAmount，不参与任何一级差额
        entity.setPoolAmount(ctx.poolAmount);
        entity.setInvoiceCount(ctx.invoiceCount);
        entity.setInvoiceVoidCount(ctx.invoiceVoidCount);
        // 凭证链（Σ班结定格）—— 与上面那条资金链对照，差多少就是没人交班的钱
        entity.setDetailAmount(ctx.shiftSumAmount);
        entity.setDeptCount(ctx.deptCount);
        entity.setDeptAmount(ctx.deptAmount);
        entity.setUnattributedAmount(ctx.unattributedAmount);
        entity.setUnassignedCount(reconcile.getUnassignedCount());
        entity.setUnassignedAmount(reconcile.getUnassignedAmount());
        entity.setReconcileStatus(Boolean.TRUE.equals(reconcile.getPassed()) ? 1 : 2);
        entity.setDiffAmount(reconcile.getMaxDiffAmount());
        entity.setDiffDetail(reconcile.getSummary());
        entity.setSettleStatus(1);
        entity.setSettleBy(UserUtils.getCurrentUser().getRealName());
        entity.setSettleTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
        if (existing == null) {
            dayMapper.insert(entity);
        } else {
            dayMapper.updateById(entity);
        }

        // 反写凭证链：班结单 → 日结单（并能双向查），同时把状态推进到"已日结"。
        // 不反写的话，交班单上永远看不出"这个班已经被哪张日结单收走了"，
        // 审核时也只能按日期盲扫 —— 日期相同但落在日结之后的班结单会被误当成本单的一部分。
        BizCashierSettlement patch = new BizCashierSettlement();
        patch.setDaySettlementId(entity.getId());
        patch.setSettleStatus(2);
        cashierMapper.update(patch, new LambdaUpdateWrapper<BizCashierSettlement>()
                .ge(BizCashierSettlement::getPeriodEnd, day.atStartOfDay())
                .lt(BizCashierSettlement::getPeriodEnd, day.plusDays(1).atStartOfDay())
                // 已审核（3）的不动：日结可重算，但已审核的班结单不该被重算顺手动到
                .ne(BizCashierSettlement::getSettleStatus, 3));
        return toDayVO(entity);
    }

    @Override
    public PageResult<DaySettlementVO> dayPage(DaySettlementQueryPageDTO dto) {
        LambdaQueryWrapper<BizDaySettlement> w = new LambdaQueryWrapper<>();
        w.eq(dto.getReconcileStatus() != null, BizDaySettlement::getReconcileStatus, dto.getReconcileStatus())
                .eq(dto.getSettleStatus() != null, BizDaySettlement::getSettleStatus, dto.getSettleStatus())
                // ⚠ 日期必须用 parseDateOrNull：LambdaQueryWrapper 的条件是"布尔 + 值"两个参数，
                // Java 会**先求值**再传进去 —— 写成 parseDate(dto.getDateStart()) 时，
                // 前端不传日期（null）也会执行 parseDate(null) 直接抛"日期不能为空"，
                // 表现为"分页接口不传日期就 500"。这是没在前端引用过的接口最容易漏的一种坏法。
                .ge(StringUtils.hasText(dto.getDateStart()), BizDaySettlement::getSettleDate, parseDateOrNull(dto.getDateStart()))
                .le(StringUtils.hasText(dto.getDateEnd()), BizDaySettlement::getSettleDate, parseDateOrNull(dto.getDateEnd()))
                // settle_date 本身唯一，这里补 id 只是统一写法（别的表同秒多行才是真问题）
                .orderByDesc(BizDaySettlement::getSettleDate)
                .orderByDesc(BizDaySettlement::getId);
        Page<BizDaySettlement> page = dayMapper.selectPage(
                new Page<>(pageNum(dto.getPageNum()), pageSize(dto.getPageSize())), w);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toDayVOList(page.getRecords()));
    }

    @Override
    public DaySettlementDetailVO getDayDetailById(Long id) {
        // C 类保留：入参是 Long（GET 参数直传），没有 DTO 承载注解；@RequestParam 已 required，此处是直调兜底
        if (id == null) {
            throw new BusinessException("日结单ID不能为空");
        }
        BizDaySettlement row = dayMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("日结单不存在");
        }
        return buildDetail(row.getSettleDate(), row);
    }

    @Override
    public DaySettlementDetailVO getDayDetailByDate(String date) {
        LocalDate day = parseDate(date);
        BizDaySettlement row = dayMapper.selectOne(new LambdaQueryWrapper<BizDaySettlement>()
                .eq(BizDaySettlement::getSettleDate, day)
                .last("LIMIT 1"));
        return buildDetail(day, row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditDaySettlement(DaySettlementAuditDTO dto) {
        BizDaySettlement row = dayMapper.selectById(dto.getId());
        if (row == null) {
            throw new BusinessException("日结单不存在");
        }
        if (Objects.equals(row.getSettleStatus(), 2)) {
            throw new BusinessException("该日结单已审核，请勿重复审核");
        }
        row.setSettleStatus(2);
        row.setAuditBy(UserUtils.getCurrentUser().getRealName());
        row.setAuditTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        row.setAuditRemark(dto.getAuditRemark());
        if (dto.getRemark() != null) {
            row.setRemark(dto.getRemark());
        }
        dayMapper.updateById(row);

        // 交班单同步置"已审核"，让收费员那边看得出"这个班已经结完、不用再来问"。
        // 按 day_settlement_id 取而不是按日期扫：日期相同但**不属于本单**的班结单
        // （日结之后才交的班）不该被这次审核顺带改成已审核。
        for (BizCashierSettlement s : cashierMapper.selectList(new LambdaQueryWrapper<BizCashierSettlement>()
                .eq(BizCashierSettlement::getDaySettlementId, row.getId()))) {
            s.setSettleStatus(3);
            s.setAuditBy(row.getAuditBy());
            s.setAuditTime(row.getAuditTime());
            s.setAuditRemark(dto.getAuditRemark());
            cashierMapper.updateById(s);
        }
    }

    @Override
    public SettlementReconcileVO reconcile(String date) {
        LocalDate day = parseDate(date);
        return reconcileOf(day, buildContext(day));
    }

    @Override
    public List<DeptIncomeVO> deptIncome(String date) {
        LocalDate day = parseDate(date);
        DayContext ctx = buildContext(day);
        return buildDeptIncomes(ctx);
    }

    /**
     * 跑一遍三级对账。日期 + 上下文（区间、班结单）都从外面传，避免这里重复查库。
     */
    private SettlementReconcileVO reconcileOf(LocalDate day, DayContext ctx) {
        List<ReconcileItemVO> items = new ArrayList<>();

        // 一级：逐张班结单，按它认领的流水集合复算
        // 左右两侧同用归集指针（本班快照也是这么算出来的），但取数路径不同：
        // 左值是交班那一刻定格的列，右值是此刻回 L3 现算 —— 中途有流水被冲正、
        // 改金额或被别的班越权认领，右值就会移动，这才是对账。
        int shiftBad = 0;
        BigDecimal shiftBadDiff = BigDecimal.ZERO;
        BigDecimal shiftRecomputedTotal = BigDecimal.ZERO;
        StringBuilder shiftBadMsg = new StringBuilder();
        for (BizCashierSettlement s : ctx.shifts) {
            BigDecimal recomputed = BigDecimal.ZERO;
            for (PaymentMethodSumVO row : cashierMapper.sumPaidBySettlement(s.getId())) {
                recomputed = recomputed.add(nz(row.getAmount()));
            }
            recomputed = recomputed.setScale(SCALE, RoundingMode.HALF_UP);
            shiftRecomputedTotal = shiftRecomputedTotal.add(recomputed);
            BigDecimal snap = nz(s.getChargeAmount());
            if (recomputed.compareTo(snap) != 0) {
                shiftBad++;
                BigDecimal d = recomputed.subtract(snap).abs();
                if (d.compareTo(shiftBadDiff) > 0) {
                    shiftBadDiff = d;
                }
                if (shiftBadMsg.length() < 400) {
                    shiftBadMsg.append(shiftBadMsg.length() > 0 ? "；" : "")
                            .append(s.getSettlementNo()).append(" 快照 ").append(snap.toPlainString())
                            .append(" 实算 ").append(recomputed.toPlainString());
                }
            }
        }
        ReconcileItemVO lv1 = new ReconcileItemVO();
        lv1.setLevel("cashierShift");
        lv1.setLevelName("一级：交班单 ↔ 该班认领的支付流水");
        lv1.setLeftLabel(ctx.shifts.size() + " 张交班单的定格金额");
        lv1.setLeftAmount(ctx.shiftSumAmount);
        lv1.setRightLabel("按本班归集指针回 L3 流水重新复算");
        shiftRecomputedTotal = shiftRecomputedTotal.setScale(SCALE, RoundingMode.HALF_UP);
        lv1.setRightAmount(shiftRecomputedTotal);
        lv1.setDiffAmount(shiftRecomputedTotal.subtract(ctx.shiftSumAmount));
        lv1.setPassed(shiftBad == 0);
        lv1.setDiffCount(shiftBad);
        lv1.setMessage(shiftBad == 0
                ? "全部 " + ctx.shifts.size() + " 张交班单与其认领的收款流水一致"
                : shiftBad + " 张交班单与当前流水不符：" + shiftBadMsg);
        items.add(lv1);

        // 二级：Σ交班单（凭证链）↔ 全院流水实收（资金链）
        // 右值扣掉"系统代收"（患者端自助缴费，cashier_id=0）：那笔钱没有哪个收银员会为它交班，
        // 混进来会让本级永远不平，从而再也没人认真看这个差额。它单列在消息里，
        // 并交由渠道对账单（支付渠道对账流水）那一层去核对，不是被丢掉。
        BigDecimal lv2Right = ctx.txChargeAmount.subtract(ctx.systemAmount).setScale(SCALE, RoundingMode.HALF_UP);
        ReconcileItemVO lv2 = new ReconcileItemVO();
        lv2.setLevel("dayVsShift");
        lv2.setLevelName("二级：Σ交班单 ↔ 全院流水实收");
        lv2.setLeftLabel("Σ交班单金额（有人认领的钱）");
        lv2.setLeftAmount(ctx.shiftSumAmount);
        lv2.setRightLabel("全院当日实收（不含患者端自助缴纳）");
        lv2.setRightAmount(lv2Right);
        BigDecimal d2 = lv2Right.subtract(ctx.shiftSumAmount).setScale(SCALE, RoundingMode.HALF_UP);
        lv2.setDiffAmount(d2);
        lv2.setPassed(d2.compareTo(BigDecimal.ZERO) == 0);
        lv2.setDiffCount(ctx.unassignedCount);
        lv2.setMessage((d2.compareTo(BigDecimal.ZERO) == 0
                ? "交班单覆盖了当日全部 " + (ctx.txChargeCount - ctx.systemCount) + " 笔人工收款"
                : "差 " + d2.toPlainString() + " 元：有 " + ctx.unassignedCount + " 笔（"
                + ctx.unassignedAmount.toPlainString() + " 元）没人交班"
                + "（忘了交班，或交完班又收了钱）")
                + (ctx.systemCount > 0 ? "；另有 " + ctx.systemCount + " 笔（"
                + ctx.systemAmount.toPlainString() + " 元）由患者端自助缴纳，不经收银台，走渠道账单核对" : ""));
        items.add(lv2);

        // 三级：Σ账单摊行（含无科室归属）↔ Σ账单单头
        // 左 = Σ科室收入 + 无科室归属（= 当日收讫账单的全部明细摊行）；右 = 同一批账单的单头应收合计。
        // **必须是两张表**：摊行对摊行是恒真断言（自己等于自己），出不了任何差异。
        // 单头与摊行各存一份金额，对不上说明"摊行漏写/多写/改过其中一边"，这是 L2 内部的真问题。
        BigDecimal deptSum = ctx.deptAmount.add(ctx.unattributedAmount).setScale(SCALE, RoundingMode.HALF_UP);
        ReconcileItemVO lv3 = new ReconcileItemVO();
        lv3.setLevel("deptAttribution");
        lv3.setLevelName("三级：科室归集 ↔ 账单单头");
        lv3.setLeftLabel("Σ科室收入 + 无科室归属（明细摊行）");
        lv3.setLeftAmount(deptSum);
        lv3.setRightLabel("当日收讫账单的单头应收合计");
        lv3.setRightAmount(ctx.billHeaderAmount);
        BigDecimal d3 = ctx.billHeaderAmount.subtract(deptSum).setScale(SCALE, RoundingMode.HALF_UP);
        lv3.setDiffAmount(d3);
        lv3.setPassed(d3.compareTo(BigDecimal.ZERO) == 0);
        lv3.setDiffCount(ctx.unattributedCount);
        lv3.setMessage(d3.compareTo(BigDecimal.ZERO) == 0
                ? (ctx.unattributedCount == 0
                ? ctx.billHeaderCount + " 张收讫账单的摊行已全部归到科室（" + ctx.deptCount
                + " 个科室），摊行合计与单头一致"
                : "金额闭合；其中 " + ctx.unattributedCount + " 行（"
                + ctx.unattributedAmount.toPlainString() + " 元）无科室归属，已单列不并入科室统计")
                : "摊行合计与账单单头差 " + d3.toPlainString() + " 元：同一批账单拆行后与单头对不上，"
                + "常见原因是摊行漏写/多写或只改了一边");
        items.add(lv3);

        // 汇总
        boolean passed = Boolean.TRUE.equals(lv1.getPassed()) && Boolean.TRUE.equals(lv2.getPassed())
                && Boolean.TRUE.equals(lv3.getPassed());
        BigDecimal maxDiff = lv1.getDiffAmount().abs().max(lv2.getDiffAmount().abs())
                .max(lv3.getDiffAmount().abs()).setScale(SCALE, RoundingMode.HALF_UP);

        SettlementReconcileVO vo = new SettlementReconcileVO();
        vo.setSettleDate(day.format(DateFormats.DATE));
        vo.setItems(items);
        vo.setPassed(passed);
        vo.setMaxDiffAmount(maxDiff);
        vo.setUnassignedCount(ctx.unassignedCount);
        vo.setUnassignedAmount(ctx.unassignedAmount);
        vo.setUnattributedAmount(ctx.unattributedAmount);
        StringBuilder summary = new StringBuilder("三级对账").append(passed ? "全部通过" : "未平")
                .append("（当日全院实收 ").append(ctx.txChargeAmount.toPlainString())
                .append(" 元、统筹记账 ").append(ctx.poolAmount.toPlainString())
                .append(" 元，").append(ctx.shifts.size()).append(" 张交班单");
        if (!passed) {
            String failMsg = items.stream()
                    .filter(i -> !Boolean.TRUE.equals(i.getPassed()))
                    .map(ReconcileItemVO::getMessage)
                    .reduce((a, b) -> a + " / " + b)
                    .orElse("");
            summary.append("；最大差异 ").append(maxDiff.toPlainString()).append(" 元；").append(failMsg);
        }
        summary.append("）");
        vo.setSummary(summary.toString());
        return vo;
    }

    /**
     * 一天的日结上下文：区间、班结单、各口径金额。
     *
     * <p><b>区间取法有讲究</b>：
     * <ul>
     *   <li><b>下界 = 各交班单 period_begin 的最小值</b>（当日无交班单时回落前一日 23:59:59）。
     *       不用"当日 00:00"是因为夜班 16:00-次日 08:00 的班结单 period_end 落在今天，
     *       但它的单子里有**昨天**的收费 —— 若独立复算按自然日算，Σ班结与复算必然对不上，
     *       而这是口径差不是真差异。用并集则两者同口径。</li>
     *   <li><b>上界 = 当日 23:59:59（自然日结束），不是 max(period_end)</b>。
     *       这一条是踩出来的：曾经用 max(period_end) 收口，结果"最后一次交班之后新收的钱"
     *       落在窗口外 —— 既不进独立复算、也进不了"未纳班结"，二级对账永远看不见它。
     *       而"交班后又收了费/忘了交班"恰恰是二级对账唯一要抓的东西，等于把唯一的用途废掉了。
     *       上界放到自然日结束，这些钱才会显式落进 {@code unassignedCount/Amount}。</li>
     * </ul>
     */
    private DayContext buildContext(LocalDate day) {
        DayContext ctx = new DayContext();
        ctx.day = day;
        ctx.shifts = shiftsOf(day);

        LocalDateTime begin;
        if (ctx.shifts.isEmpty()) {
            begin = day.atStartOfDay().minusSeconds(1);
        } else {
            begin = ctx.shifts.stream().map(BizCashierSettlement::getPeriodBegin)
                    .filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(day.atStartOfDay().minusSeconds(1));
        }
        ctx.begin = begin;
        ctx.end = day.atTime(23, 59, 59);

        // 凭证链：Σ交班单定格金额（不重新汇总渠道，班结当时已经点过一遍）
        for (BizCashierSettlement s : ctx.shifts) {
            ctx.shiftSumAmount = ctx.shiftSumAmount.add(nz(s.getChargeAmount()));
        }
        ctx.shiftSumAmount = ctx.shiftSumAmount.setScale(SCALE, RoundingMode.HALF_UP);

        // 资金链：全院支付流水现算（区间 = ctx.begin / ctx.end，取法见上面的说明）
        ctx.txChargeAmount = nz(dayMapper.sumPaidAmount(ctx.begin, ctx.end)).setScale(SCALE, RoundingMode.HALF_UP);
        ctx.txChargeCount = (int) dayMapper.countPaid(ctx.begin, ctx.end);
        ctx.billCount = (int) dayMapper.countPaidBills(ctx.begin, ctx.end);
        CountAmountVO refund = dayMapper.sumRefund(ctx.begin, ctx.end);
        ctx.txRefundCount = cntOf(refund.getCnt());
        ctx.txRefundAmount = nz(refund.getAmount()).setScale(SCALE, RoundingMode.HALF_UP);
        for (PaymentMethodSumVO row : dayMapper.sumPaidByPaymentMethod(ctx.begin, ctx.end)) {
            ctx.buckets.add(row.getPaymentMethod(), nz(row.getAmount()), cntOf(row.getCnt()));
        }
        ctx.poolAmount = nz(dayMapper.sumPoolAmount(ctx.begin, ctx.end)).setScale(SCALE, RoundingMode.HALF_UP);

        // 交班归集缺口
        CountAmountVO unassigned = dayMapper.sumUnassigned(ctx.begin, ctx.end);
        ctx.unassignedCount = cntOf(unassigned.getCnt());
        ctx.unassignedAmount = nz(unassigned.getAmount()).setScale(SCALE, RoundingMode.HALF_UP);
        CountAmountVO system = dayMapper.sumSystemCollected(ctx.begin, ctx.end);
        ctx.systemCount = cntOf(system.getCnt());
        ctx.systemAmount = nz(system.getAmount()).setScale(SCALE, RoundingMode.HALF_UP);

        // 账单链：当日收讫账单的单头 ↔ 摊行 ↔ 科室
        List<DeptAmountSumVO> deptRows = dayMapper.sumDetailByDept(ctx.begin, ctx.end);
        ctx.deptCount = deptRows.size();
        BigDecimal deptSum = BigDecimal.ZERO;
        for (DeptAmountSumVO row : deptRows) {
            deptSum = deptSum.add(nz(row.getAmount()));
        }
        ctx.deptAmount = deptSum.setScale(SCALE, RoundingMode.HALF_UP);
        CountAmountVO unattr = dayMapper.sumDetailUnattributed(ctx.begin, ctx.end);
        ctx.unattributedCount = cntOf(unattr.getCnt());
        ctx.unattributedAmount = nz(unattr.getAmount()).setScale(SCALE, RoundingMode.HALF_UP);
        CountAmountVO all = dayMapper.sumDetailAll(ctx.begin, ctx.end);
        ctx.detailAllAmount = nz(all.getAmount()).setScale(SCALE, RoundingMode.HALF_UP);
        CountAmountVO header = dayMapper.sumBillHeader(ctx.begin, ctx.end);
        ctx.billHeaderCount = cntOf(header.getCnt());
        ctx.billHeaderAmount = nz(header.getAmount()).setScale(SCALE, RoundingMode.HALF_UP);
        ctx.deptRows = deptRows;

        // 票据：票跟着账单走（当日收讫账单所开的票）
        InvoiceCountVO invoice = dayMapper.sumInvoice(ctx.begin, ctx.end);
        ctx.invoiceCount = cntOf(invoice.getCnt());
        ctx.invoiceVoidCount = cntOf(invoice.getVoidCnt());
        return ctx;
    }

    private List<BizCashierSettlement> shiftsOf(LocalDate day) {
        return cashierMapper.selectList(new LambdaQueryWrapper<BizCashierSettlement>()
                .ge(BizCashierSettlement::getPeriodEnd, day.atStartOfDay())
                .lt(BizCashierSettlement::getPeriodEnd, day.plusDays(1).atStartOfDay())
                .orderByAsc(BizCashierSettlement::getPeriodEnd)
                .orderByAsc(BizCashierSettlement::getId));
    }

    private List<DeptIncomeVO> buildDeptIncomes(DayContext ctx) {
        BigDecimal denominator = ctx.detailAllAmount;
        List<DeptIncomeVO> list = new ArrayList<>();
        for (DeptAmountSumVO row : ctx.deptRows) {
            DeptIncomeVO vo = new DeptIncomeVO();
            vo.setDeptId(row.getDeptId());
            vo.setDeptName(row.getDeptName());
            vo.setItemCount(cntOf(row.getCnt()));
            BigDecimal amount = nz(row.getAmount()).setScale(SCALE, RoundingMode.HALF_UP);
            vo.setAmount(amount);
            vo.setRatio(ratio(amount, denominator));
            vo.setUnattributed(false);
            list.add(vo);
        }
        if (ctx.unattributedCount > 0 || ctx.unattributedAmount.signum() != 0) {
            DeptIncomeVO vo = new DeptIncomeVO();
            vo.setDeptName("无科室归属");
            vo.setItemCount(ctx.unattributedCount);
            vo.setAmount(ctx.unattributedAmount);
            vo.setRatio(ratio(ctx.unattributedAmount, denominator));
            vo.setUnattributed(true);
            list.add(vo);
        }
        return list;
    }

    private DaySettlementDetailVO buildDetail(LocalDate day, BizDaySettlement row) {
        DayContext ctx = buildContext(day);
        DaySettlementDetailVO vo = new DaySettlementDetailVO();
        if (row != null) {
            vo.setSettlement(toDayVO(row));
        }
        List<CashierSettlementVO> shiftVOs = new ArrayList<>();
        for (BizCashierSettlement s : ctx.shifts) {
            shiftVOs.add(toCashierVO(s));
        }
        vo.setShifts(shiftVOs);
        vo.setReconcile(reconcileOf(day, ctx));
        vo.setDeptIncomes(buildDeptIncomes(ctx));
        vo.setTotalDetailAmount(ctx.detailAllAmount);
        return vo;
    }

    private String nextCashierNo() {
        // 与 EmrServiceImpl 的取号同理：按"当天已用序号"取，不用进程内自增 ——
        // 进程内自增在服务重启后当天第一单必然撞唯一索引。
        String prefix = "JS" + LocalDateTime.now().format(DateFormats.COMPACT_DATE);
        long used = cashierMapper.countByNoPrefix(prefix);
        return prefix + String.format("%04d", (used + 1) % 10000);
    }

    private long countCashier(int status) {
        return cashierMapper.selectCount(new LambdaQueryWrapper<BizCashierSettlement>()
                .eq(BizCashierSettlement::getSettleStatus, status));
    }

    private long countDay(int status) {
        return dayMapper.selectCount(new LambdaQueryWrapper<BizDaySettlement>()
                .eq(BizDaySettlement::getSettleStatus, status));
    }

    /**
     * 支付渠道分桶（班结、日结共用同一份口径）。
     *
     * <p>之所以抽出来：这两处要是各写一遍 {@code switch}，改渠道时必然漏一处，
     * 而漏掉的那一处**不报错**，只是某个渠道的钱凭空少一截、{@code unknownPay} 多一截。
     *
     * <p>{@code unknownPay} 是恒等式现金+微信+支付宝+个账+余额+未知 = amount 的补数：
     * 支付方式缺失，以及 6-银行卡 / 7-转账（本层没有对应列）都归它，绝不并进别的渠道。
     */
    private static class PayBuckets {
        BigDecimal cash = BigDecimal.ZERO;
        BigDecimal wechat = BigDecimal.ZERO;
        BigDecimal alipay = BigDecimal.ZERO;
        BigDecimal insurance = BigDecimal.ZERO;
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal unknownPay = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        int count;

        void add(Integer paymentMethod, BigDecimal value, int cnt) {
            amount = amount.add(value);
            count += cnt;
            if (paymentMethod == null) {
                unknownPay = unknownPay.add(value);
                return;
            }
            switch (paymentMethod) {
                case PAY_CASH -> cash = cash.add(value);
                case PAY_WECHAT -> wechat = wechat.add(value);
                case PAY_ALIPAY -> alipay = alipay.add(value);
                case PAY_INSURANCE -> insurance = insurance.add(value);
                case PAY_BALANCE -> balance = balance.add(value);
                default -> unknownPay = unknownPay.add(value);
            }
        }
    }

    /**
     * 一天的日结上下文（区间 + 三条链各自的口径金额），一次构建多处复用。
     */
    private static class DayContext {
        /**
         * 资金链（L3 全院支付流水现算）：今天到底进了多少钱
         */
        final PayBuckets buckets = new PayBuckets();
        LocalDate day;
        LocalDateTime begin;
        LocalDateTime end;
        List<BizCashierSettlement> shifts = List.of();
        int txChargeCount;
        BigDecimal txChargeAmount = BigDecimal.ZERO;
        int txRefundCount;
        BigDecimal txRefundAmount = BigDecimal.ZERO;
        int billCount;
        BigDecimal poolAmount = BigDecimal.ZERO;

        /**
         * 凭证链（L4 班结单定格合计）：有人认领的钱
         */
        BigDecimal shiftSumAmount = BigDecimal.ZERO;

        /**
         * 系统代收（患者端自助缴费，cashier_id=0）：钱是真的，但没有收银员为它交班
         */
        int systemCount;
        BigDecimal systemAmount = BigDecimal.ZERO;

        /**
         * 真人经手但没人交班的流水（二级对账的差额本体）
         */
        int unassignedCount;
        BigDecimal unassignedAmount = BigDecimal.ZERO;

        /**
         * 账单链（L2 当日收讫账单）：单头 ↔ 摊行 ↔ 科室
         */
        int billHeaderCount;
        BigDecimal billHeaderAmount = BigDecimal.ZERO;
        BigDecimal detailAllAmount = BigDecimal.ZERO;
        int deptCount;
        BigDecimal deptAmount = BigDecimal.ZERO;
        int unattributedCount;
        BigDecimal unattributedAmount = BigDecimal.ZERO;
        List<DeptAmountSumVO> deptRows = List.of();

        int invoiceCount;
        int invoiceVoidCount;
    }
}
