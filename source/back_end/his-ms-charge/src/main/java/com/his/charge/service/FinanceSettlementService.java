package com.his.charge.service;


import com.his.charge.dto.*;
import com.his.charge.entity.BizDaySettlement;
import com.his.charge.vo.*;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 财务班结 / 日结 / 三级对账（G8）。
 */
public interface FinanceSettlementService {

    /**
     * 收费员交班（班结）。一次交班生成一条快照，金额即锁定。
     *
     * <p>出参是**新建的交班单**而不是 ID：ID 是雪花值（19 位），
     * 裸 {@code Long} 出参到前端会被 JS 的 Number 截断，调用方拿到的 ID 是错的且不报错。
     *
     * @return 新建的交班单
     */
    CashierSettlementVO handover(CashierHandoverDTO dto);

    /**
     * 交班前试算：当前收费员「上次交班 → 现在」的系统账，不落库、不认领流水。
     *
     * <p>给收费台交班弹框用 —— 点钞之前先看到本班系统现金是多少，差异在提交前
     * 就摆平，而不是确认被拒后从报错里猜。区间与行集口径同 {@link #handover}。
     *
     * @return 试算结果（含系统现金 cashAmount）
     */
    CashierHandoverPreviewVO handoverPreview();

    /**
     * 交班单分页
     */
    PageResult<CashierSettlementVO> cashierPage(CashierSettlementPageQueryDTO dto);

    /**
     * 交班单详情
     */
    CashierSettlementVO getCashierById(Long id);

    /**
     * 各状态计数
     */
    SettlementStatusCountVO statusCount();

    /**
     * 执行日结（生成，或覆盖重算"待审核"的草稿）。
     *
     * @return 落库后的日结单（ID 同 {@link #handover} 的理由，不裸回 Long）
     */
    DaySettlementVO runDaySettlement(DaySettlementRunDTO dto);

    /**
     * 日结单分页
     */
    PageResult<DaySettlementVO> dayPage(DaySettlementPageQueryDTO dto);

    /**
     * 日结单详情（日结单 + 当日班结单 + 三级对账 + 科室收入）
     */
    DaySettlementDetailVO getDayDetailById(Long id);

    /**
     * 按日期取日结详情：尚未日结时返回"试算"结果（settlement 为 null，其余照常给出）。
     */
    DaySettlementDetailVO getDayDetailByDate(String date);

    /**
     * 审核日结单（审核后不可重算）
     */
    void auditDaySettlement(DaySettlementAuditDTO dto);

    /**
     * 三级对账（可独立于日结单随时查）
     */
    SettlementReconcileVO reconcile(String date);

    /**
     * 科室收入（末行是无科室归属的差异项）
     */
    List<DeptIncomeVO> deptIncome(String date);
}
