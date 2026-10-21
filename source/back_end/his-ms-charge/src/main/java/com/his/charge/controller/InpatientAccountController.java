package com.his.charge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.InpatientSettlementUpsertDTO;
import com.his.spi.dto.PrepayPageQueryDTO;
import com.his.spi.dto.PrepayUpsertDTO;
import com.his.charge.service.InpatientAccountService;
import com.his.charge.vo.*;
import com.his.spi.vo.PrepayBalanceVO;
import com.his.spi.vo.PrepayVO;
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
     * 账务查看：三个入口任一即可读
     *   - charge:arrearsControl:list  欠费管控页
     *   - ipd:inpatient:list         入出院管理（医生/护士站）
     *   - ipd:settlement:list        住院结算页（财务岗）—— 住院结算员只挂本页菜单即可，不再借欠费管控的码
     */
    private static final String VIEW = "hasAnyAuthority('charge:arrearsControl:list', 'ipd:inpatient:list', 'ipd:settlement:list')";

    /**
     * 收预交金：住院结算页用自己的按钮码，入出院管理页沿用 ipd:inpatient:add。
     * 两侧都认是为了让「财务岗办住院收费」不必借医护页面的码 —— 借来的码收不回来（一收就把入出院管理的新增一起关掉）。
     */
    private static final String SAVE_PREPAY = "hasAnyAuthority('ipd:settlement:add', 'ipd:inpatient:add')";

    /**
     * 办理出院结算（自费与医保同一入口，由入参 settleMode 分流）：同上，住院结算页用 ipd:settlement:edit。
     */
    private static final String DO_SETTLE = "hasAnyAuthority('ipd:settlement:edit', 'ipd:inpatient:edit')";

    private final InpatientAccountService inpatientAccountService;

    /**
     * 分页查询某次住院的预交金收退流水，按收退时间倒序。
     * 用于住院结算工作区查看该患者历次充值与退款记录。
     */
    @PreAuthorize(VIEW)
    @Operation(summary = "预交金流水分页（按收退时间倒序）")
    @GetMapping("/prepay/listPage")
    public Result<IPage<PrepayVO>> prepayListPage(@Valid PrepayPageQueryDTO query) {
        return Result.success(inpatientAccountService.prepayListPage(query));
    }

    /**
     * 取住院预交金账户的充值合计、退款合计与当前余额。
     * 用于住院结算工作区账户卡片展示余额，余额由资金账户流水现算。
     */
    @PreAuthorize(VIEW)
    @Operation(summary = "预交金账户（充值/退款合计与当前余额，余额=资金账户 SUM）")
    @GetMapping("/prepay/balance")
    public Result<PrepayBalanceVO> balance(@RequestParam Long admissionId) {
        return Result.success(inpatientAccountService.balance(admissionId));
    }

    /**
     * 收取或退还住院预交金（L3 资金账户收款/退款动作），金额一律正数、方向由类型决定，退款可摊成多笔流水。
     * 用于住院结算工作区办理充值或退预交金。
     */
    @PreAuthorize(SAVE_PREPAY)
    @Operation(summary = "收预交金（充值 / 退款；金额一律传正数，方向由 prepayType 决定；退款可能摊成多笔流水）")
    @PostMapping("/prepay/save")
    public Result<List<PrepayVO>> savePrepay(@Valid @RequestBody PrepayUpsertDTO dto) {
        return Result.success("预交金已登记", inpatientAccountService.savePrepay(dto));
    }

    /**
     * 按天汇总住院日清单，事实来源是 L1 记账行的当日净额。
     * 用于住院结算工作区向患者展示逐日费用明细。
     */
    @PreAuthorize(VIEW)
    @Operation(summary = "住院日清单（按天汇总；事实来源是 L1 记账行净额）")
    @GetMapping("/dailyBill")
    public Result<DailyBillVO> dailyBill(@RequestParam Long admissionId,
                                         @RequestParam(required = false) String beginDate,
                                         @RequestParam(required = false) String endDate) {
        return Result.success(inpatientAccountService.dailyBill(admissionId, beginDate, endDate));
    }

    /**
     * 出院结算试算：算出 L2 账单应缴，叠加住院账户余额的抵扣、退差与欠费金额，不落库。
     * 用于住院结算工作区点击正式结算前的金额预览。settleMode 区分自费与医保口径。
     */
    @PreAuthorize(VIEW)
    @Operation(summary = "出院结算试算（L2 账单应缴 + 住院账户余额抵扣/退差/欠费）")
    @GetMapping("/settlement/preview")
    public Result<InpatientSettlementPreviewVO> preview(@RequestParam Long admissionId,
                                                        @RequestParam Integer settleMode) {
        return Result.success(inpatientAccountService.preview(admissionId, settleMode));
    }

    /**
     * 办理出院结算（L2 出账单 + 账户余额抵扣 + 退差收款动作）；欠费时账单停留在未付清。
     * 用于住院结算工作区确认结算，自费与医保同一入口由入参分流。
     */
    @PreAuthorize(DO_SETTLE)
    @Operation(summary = "办理出院结算（出 L2 账单 + 余额抵扣 + 退差；欠费时账单留在未付清）")
    @PostMapping("/settlement/settle")
    public Result<InpatientSettlementVO> settle(@Valid @RequestBody InpatientSettlementUpsertDTO dto) {
        return Result.success("住院结算已完成", inpatientAccountService.settle(dto));
    }

    /**
     * 取该次住院当前有效的出院结算账单，未结算时返回空。
     * 用于住院结算工作区回显已完成的结算结果。
     */
    @PreAuthorize(VIEW)
    @Operation(summary = "当前有效的出院结算账单（未结算返回 null）")
    @GetMapping("/settlement/detail")
    public Result<InpatientSettlementVO> settlementDetail(@RequestParam Long admissionId) {
        return Result.success(inpatientAccountService.settlementDetail(admissionId));
    }

    /**
     * 住院账务概览：账户余额、已发生费用与是否欠费；欠费只提示不阻断业务。
     * 用于住院结算工作区与入出院管理页顶部的账务状态卡。
     */
    @PreAuthorize(VIEW)
    @Operation(summary = "住院账务概览（账户余额 / 已发生费用 / 是否欠费；欠费只提示不阻断）")
    @GetMapping("/summary")
    public Result<InpatientAccountSummaryVO> summary(@RequestParam Long admissionId) {
        return Result.success(inpatientAccountService.summary(admissionId));
    }
}

