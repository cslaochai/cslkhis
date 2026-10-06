package com.his.charge.controller;

import com.his.charge.dto.InpatientSettlementUpsertDTO;
import com.his.charge.dto.PrepayQueryPageDTO;
import com.his.charge.dto.PrepayUpsertDTO;
import com.his.charge.service.InpatientAccountService;
import com.his.charge.service.InpatientSettlementGateway;
import com.his.charge.vo.DailyBillVO;
import com.his.charge.vo.InpatientAccountSummaryVO;
import com.his.charge.vo.InpatientSettlementPreviewVO;
import com.his.charge.vo.InpatientSettlementVO;
import com.his.charge.vo.PrepayBalanceVO;
import com.his.charge.vo.PrepayVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 住院账务（预交金 / 日清单 / 出院结算 / 欠费提示）。
 *
 * <p>两个容易被误会的点：
 * <ol>
 *   <li>{@code /summary} 是 GET 但<b>可能写一条欠费告警</b>（同 P1 的 {@code /execPendingList}）：
 *       告警按"同一入院一天一条"去重，医生站每次打开页面都会查，不去重会刷屏。</li>
 *   <li><b>欠费不阻断任何诊疗动作</b>：本控制器没有任何"欠费则拒绝"的分支，
 *       唯一的拦截在出院侧（没结算不允许出院，见 {@code InpatientSettlementGateway}）。</li>
 * </ol>
 *
 * <p><b>鉴权一律标在方法上，不标在类上</b>：类级注解会静默覆盖所有没写自己注解的方法，
 * 医生站/护士站的欠费提示与前台的预交金就会各拿各的码、互相踢 403（G5b 踩过）。
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

    private final InpatientAccountService accountService;

    @PreAuthorize(VIEW)
    @Operation(summary = "预交金流水分页（按收退时间倒序）")
    @GetMapping("/prepay/listPage")
    public Result<IPage<PrepayVO>> prepayListPage(@Valid PrepayQueryPageDTO query) {
        return Result.success(accountService.prepayListPage(query));
    }

    @PreAuthorize(VIEW)
    @Operation(summary = "预交金账户（充值/退款合计与当前余额，余额=资金账户 SUM）")
    @GetMapping("/prepay/balance")
    public Result<PrepayBalanceVO> balance(@RequestParam Long admissionId) {
        return Result.success(accountService.balance(admissionId));
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:add')")
    @Operation(summary = "收预交金（充值 / 退款；金额一律传正数，方向由 prepayType 决定；退款可能摊成多笔流水）")
    @PostMapping("/prepay/save")
    public Result<List<PrepayVO>> savePrepay(@Valid @RequestBody PrepayUpsertDTO dto) {
        return Result.success("预交金已登记", accountService.savePrepay(dto));
    }

    @PreAuthorize(VIEW)
    @Operation(summary = "住院日清单（按天汇总；事实来源是 L1 记账行净额）")
    @GetMapping("/dailyBill")
    public Result<DailyBillVO> dailyBill(@RequestParam Long admissionId,
                                         @RequestParam(required = false) String beginDate,
                                         @RequestParam(required = false) String endDate) {
        return Result.success(accountService.dailyBill(admissionId, beginDate, endDate));
    }

    @PreAuthorize(VIEW)
    @Operation(summary = "出院结算试算（L2 账单应缴 + 住院账户余额抵扣/退差/欠费）")
    @GetMapping("/settlement/preview")
    public Result<InpatientSettlementPreviewVO> preview(@RequestParam Long admissionId,
                                                        @RequestParam(required = false) Integer settleMode) {
        return Result.success(accountService.preview(admissionId, settleMode));
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "办理出院结算（出 L2 账单 + 余额抵扣 + 退差；欠费时账单留在未付清）")
    @PostMapping("/settlement/settle")
    public Result<InpatientSettlementVO> settle(@Valid @RequestBody InpatientSettlementUpsertDTO dto) {
        return Result.success("住院结算已完成", accountService.settle(dto));
    }

    @PreAuthorize(VIEW)
    @Operation(summary = "当前有效的出院结算账单（未结算返回 null）")
    @GetMapping("/settlement/detail")
    public Result<InpatientSettlementVO> settlementDetail(@RequestParam Long admissionId) {
        return Result.success(accountService.settlementDetail(admissionId));
    }

    @PreAuthorize(VIEW)
    @Operation(summary = "住院账务概览（账户余额 / 已发生费用 / 是否欠费；欠费只提示不阻断）")
    @GetMapping("/summary")
    public Result<InpatientAccountSummaryVO> summary(@RequestParam Long admissionId) {
        return Result.success(accountService.summary(admissionId));
    }
}

