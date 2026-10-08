package com.his.charge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.InpatientSettlementUpsertDTO;
import com.his.charge.dto.PrepayQueryPageDTO;
import com.his.charge.dto.PrepayUpsertDTO;
import com.his.charge.service.InpatientAccountService;
import com.his.charge.vo.*;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住院账务（预交金 / 日清单 / 出院结算 / 欠费提示）。
 */
@Tag(name = "住院账务（预交金 / 日清单 / 结算）")
@RestController
@RequestMapping("/charge/inpatient/account")
@RequiredArgsConstructor
public class InpatientAccountController {

    /**
     * 账务查看：欠费管控页与住院工作站任一入口都可读
     */
    private static final String VIEW = "hasAnyAuthority('charge:arrearsControl:list', 'ipd:inpatient:list')";

    private final InpatientAccountService inpatientAccountService;

    @PreAuthorize(VIEW)
    @Operation(summary = "预交金流水分页（按收退时间倒序）")
    @GetMapping("/prepay/listPage")
    public Result<IPage<PrepayVO>> prepayListPage(@Valid PrepayQueryPageDTO query) {
        return Result.success(inpatientAccountService.prepayListPage(query));
    }

    @PreAuthorize(VIEW)
    @Operation(summary = "预交金账户（充值/退款合计与当前余额，余额=资金账户 SUM）")
    @GetMapping("/prepay/balance")
    public Result<PrepayBalanceVO> balance(@RequestParam Long admissionId) {
        return Result.success(inpatientAccountService.balance(admissionId));
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:add')")
    @Operation(summary = "收预交金（充值 / 退款；金额一律传正数，方向由 prepayType 决定；退款可能摊成多笔流水）")
    @PostMapping("/prepay/save")
    public Result<List<PrepayVO>> savePrepay(@Valid @RequestBody PrepayUpsertDTO dto) {
        return Result.success("预交金已登记", inpatientAccountService.savePrepay(dto));
    }

    @PreAuthorize(VIEW)
    @Operation(summary = "住院日清单（按天汇总；事实来源是 L1 记账行净额）")
    @GetMapping("/dailyBill")
    public Result<DailyBillVO> dailyBill(@RequestParam Long admissionId,
                                         @RequestParam(required = false) String beginDate,
                                         @RequestParam(required = false) String endDate) {
        return Result.success(inpatientAccountService.dailyBill(admissionId, beginDate, endDate));
    }

    @PreAuthorize(VIEW)
    @Operation(summary = "出院结算试算（L2 账单应缴 + 住院账户余额抵扣/退差/欠费）")
    @GetMapping("/settlement/preview")
    public Result<InpatientSettlementPreviewVO> preview(@RequestParam Long admissionId,
                                                        @RequestParam(required = false) Integer settleMode) {
        return Result.success(inpatientAccountService.preview(admissionId, settleMode));
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "办理出院结算（出 L2 账单 + 余额抵扣 + 退差；欠费时账单留在未付清）")
    @PostMapping("/settlement/settle")
    public Result<InpatientSettlementVO> settle(@Valid @RequestBody InpatientSettlementUpsertDTO dto) {
        return Result.success("住院结算已完成", inpatientAccountService.settle(dto));
    }

    @PreAuthorize(VIEW)
    @Operation(summary = "当前有效的出院结算账单（未结算返回 null）")
    @GetMapping("/settlement/detail")
    public Result<InpatientSettlementVO> settlementDetail(@RequestParam Long admissionId) {
        return Result.success(inpatientAccountService.settlementDetail(admissionId));
    }

    @PreAuthorize(VIEW)
    @Operation(summary = "住院账务概览（账户余额 / 已发生费用 / 是否欠费；欠费只提示不阻断）")
    @GetMapping("/summary")
    public Result<InpatientAccountSummaryVO> summary(@RequestParam Long admissionId) {
        return Result.success(inpatientAccountService.summary(admissionId));
    }
}

