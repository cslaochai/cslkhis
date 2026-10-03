package com.his.charge.controller;

import com.his.charge.dto.*;
import com.his.charge.service.FinanceSettlementService;
import com.his.charge.vo.*;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 财务班结 / 日结 / 三级对账（G8）。
 *
 * <p>⚠ 与 {@code /appoint/dayEndSettle} 无关：那个做的是门诊号源状态结转（把昨天的"候诊中"
 * 收成未就诊），属门诊号源域；这里是**财务结账**。两者同叫"日结"但没有任何关系。
 *
 * <p>⚠ 鉴权口径：本工程 {@code SecurityConfig} 仅 {@code anyRequest().authenticated()}，
 * 菜单上的 {@code finance:settlement:list} 只控可见性，不构成写权限。
 * 写接口的操作人一律服务端从登录态取（{@code UserUtils}），不信前端传的身份。
 *
 * <p>出参一律 VO：实体里有 {@code Long id}（雪花 19 位），前端 JS 的 Number 只有 53 位精度，
 * 裸实体出去前端拿到的 ID 是错值且不报错。
 */
@Tag(name = "财务班结日结")
@RestController
@RequestMapping("/settlement")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('finance:settlement:list')")
public class FinanceSettlementController {

    private final FinanceSettlementService financeSettlementService;

    // 班结

    @PreAuthorize("hasAuthority('finance:settlement:edit')")
    @Operation(summary = "收费员交班（班结）")
    @PostMapping("/handover")
    public Result<CashierSettlementVO> handover(@Valid @RequestBody CashierHandoverDTO dto) {
        return Result.success("交班成功", financeSettlementService.handover(dto));
    }

    @Operation(summary = "交班单分页查询")
    @PostMapping("/cashierListPage")
    public Result<PageResult<CashierSettlementVO>> cashierListPage(
            @Valid @RequestBody CashierSettlementQueryPageDTO queryDTO) {
        return Result.success(financeSettlementService.cashierPage(queryDTO));
    }

    @Operation(summary = "交班单详情")
    @GetMapping("/getCashierById")
    public Result<CashierSettlementVO> getCashierById(@RequestParam Long settlementId) {
        return Result.success(financeSettlementService.getCashierById(settlementId));
    }

    @Operation(summary = "财务日结各状态计数")
    @GetMapping("/statusCount")
    public Result<SettlementStatusCountVO> statusCount() {
        return Result.success(financeSettlementService.statusCount());
    }

    // 日结

    @PreAuthorize("hasAuthority('finance:settlement:edit')")
    @Operation(summary = "执行日结（生成或重算待审核的草稿）")
    @PostMapping("/runDaySettlement")
    public Result<DaySettlementVO> runDaySettlement(@Valid @RequestBody DaySettlementRunDTO dto) {
        return Result.success("日结完成", financeSettlementService.runDaySettlement(dto));
    }

    @Operation(summary = "日结单分页查询")
    @PostMapping("/dayListPage")
    public Result<PageResult<DaySettlementVO>> dayListPage(
            @Valid @RequestBody DaySettlementQueryPageDTO queryDTO) {
        return Result.success(financeSettlementService.dayPage(queryDTO));
    }

    @Operation(summary = "日结单详情（含交班单、三级对账、科室收入）")
    @GetMapping("/getDayDetailById")
    public Result<DaySettlementDetailVO> getDayDetailById(@RequestParam Long settlementId) {
        return Result.success(financeSettlementService.getDayDetailById(settlementId));
    }

    @Operation(summary = "按日期查看日结详情（未日结时返回试算结果）")
    @GetMapping("/getDayDetailByDate")
    public Result<DaySettlementDetailVO> getDayDetailByDate(@RequestParam String date) {
        return Result.success(financeSettlementService.getDayDetailByDate(date));
    }

    @PreAuthorize("hasAuthority('finance:settlement:edit')")
    @Operation(summary = "审核日结单")
    @PostMapping("/dayAudit")
    public Result<Void> dayAudit(@Valid @RequestBody DaySettlementAuditDTO dto) {
        financeSettlementService.auditDaySettlement(dto);
        return Result.success("审核成功", null);
    }

    // 对账 / 科室收入

    @Operation(summary = "三级对账（可独立于日结单随时查）")
    @GetMapping("/reconcile")
    public Result<SettlementReconcileVO> reconcile(@RequestParam String date) {
        return Result.success(financeSettlementService.reconcile(date));
    }

    @Operation(summary = "科室收入（末行为无科室归属）")
    @GetMapping("/deptIncome")
    public Result<List<DeptIncomeVO>> deptIncome(@RequestParam String date) {
        return Result.success(financeSettlementService.deptIncome(date));
    }
}
