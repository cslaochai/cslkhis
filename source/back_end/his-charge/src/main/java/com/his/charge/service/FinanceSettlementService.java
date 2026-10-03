package com.his.charge.service;

import com.his.charge.dto.*;
import com.his.charge.entity.BizDaySettlement;
import com.his.charge.vo.*;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 财务班结 / 日结 / 三级对账（G8）。
 *
 * <p>三级对账的定义见 {@link BizDaySettlement} 类注释。
 * 一句话：**班结 ↔ 单据、日结 ↔ 班结、日结 ↔ 明细**，三级都平才算平。
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
     * 交班单分页
     */
    PageResult<CashierSettlementVO> cashierPage(CashierSettlementQueryPageDTO dto);

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
    PageResult<DaySettlementVO> dayPage(DaySettlementQueryPageDTO dto);

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
