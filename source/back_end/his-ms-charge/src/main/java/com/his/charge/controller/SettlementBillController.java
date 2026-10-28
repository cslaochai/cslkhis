package com.his.charge.controller;

import com.his.spi.vo.BizSettlementBillVO;


import com.his.charge.dto.*;
import com.his.spi.dto.BillPayDTO;
import com.his.charge.service.PaymentService;
import com.his.charge.service.SettlementBillService;
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
 * 收费结算控制器：待结算记账行的出账与作废（结算层），以及账单收款的收款与退费（支付层）接口入口。
 */
@Tag(name = "结算账单")
@RestController
@RequestMapping("/charge/settlementBill")
@RequiredArgsConstructor
public class SettlementBillController {

    private final SettlementBillService settlementBillService;
    private final PaymentService paymentService;

    /**
     * 取某次就诊下尚未出账的记账行与净额，并带上该就诊已有账单的差额。
     * 用于收费台「就诊结算工作台」列出可结算的行，供收银员勾选进哪几行进下一张账单。
     */
    @Operation(summary = "结算台候选：某次就诊下待结算的记账行 + 净额")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @GetMapping("/listPendingFees")
    public Result<PendingFeeVO> listPendingFees(@RequestParam Integer encounterType,
                                                @RequestParam Long encounterId) {
        return Result.success(settlementBillService.pendingFees(encounterType, encounterId));
    }

    /**
     * 出账试算：按选中的记账行现算合计、院内优惠、医保统筹与个账拆分、应缴，不落任何数据。
     * 用于收费台工作台出账前的试算按钮，与真正出账共用同一份算法，两边数必然一致。
     */
    @Operation(summary = "出账试算：选中记账行的应收/优惠/医保 split/应缴（不落库）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/settlePreview")
    public Result<BillPreviewVO> settlePreview(@Valid @RequestBody BillSettleUpsertDTO dto) {
        return Result.success(settlementBillService.previewSettlement(dto));
    }

    /**
     * 结算出账：把选中的记账行锁成一张账单，金额由服务端按行现算，不接受前端传入。
     * 用于收费台工作台确认试算后的出账动作，是记账层通向结算层、收款得以开始的唯一写入口。
     */
    @Operation(summary = "结算出账：把待结算记账行锁成一张账单")
    @PreAuthorize("hasAuthority('finance:bill:edit')")
    @PostMapping("/settle")
    public Result<BizSettlementBillVO> settle(@Valid @RequestBody BillSettleUpsertDTO settleUpsertDTO) {
        return Result.success(settlementBillService.settleVO(settleUpsertDTO));
    }

    /**
     * 取消结算：账单作废并把名下记账行放回待结算，已经收过钱的账单一律拒绝。
     * 用于「结算账单」台账上出账出错但还没收钱时的作废按钮，避免用改账的方式掩盖错账。
     */
    @Operation(summary = "取消结算（账单作废 + 解锁记账行，已有收款的拒绝）")
    @PreAuthorize("hasAuthority('finance:bill:edit')")
    @PostMapping("/voidBill")
    public Result<Void> voidBill(@Valid @RequestBody BillVoidDTO dto) {
        settlementBillService.voidBill(dto);
        return Result.success(null);
    }

    /**
     * 收款：一张账单一次提交多笔、多渠道的收款，每笔各写一行流水，是否付清由流水比出来。
     * 用于收费台收款弹框确认到账的环节（现金当面点收、扫码与余额、医保个账同走这一口）。
     */
    @Operation(summary = "收款（一次可提交多笔、多渠道）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/pay")
    public Result<List<BizPaymentTxnVO>> pay(@Valid @RequestBody BillPayDTO dto) {
        return Result.success(paymentService.pay(dto));
    }

    /**
     * 按账单退费：应收侧红冲记账行、资金侧按原收款流水逐笔原路退回，两步同事务。
     * 供冲正一笔已收款项的业务链路调用（退费申请审核通过后的执行即由此落钱），金额只取账单事实不由页面填。
     */
    @Operation(summary = "按账单退费（红冲记账行 + 逐笔原路退回）")
    @PreAuthorize("hasAuthority('finance:refund:list')")
    @PostMapping("/refund")
    public Result<List<BizPaymentTxnVO>> refund(@Valid @RequestBody BillRefundDTO dto) {
        return Result.success(paymentService.refund(dto));
    }

    /**
     * 收费台首屏待收费榜：按就诊汇总，待出账的记账行应收与已出账单的未收差额分列给出。
     * 用于收费台查询卡的主表格，收银员据此决定先处理哪次就诊。
     */
    @Operation(summary = "收费台首屏：按就诊汇总的待收费榜（待出账应收 + 未收齐账单差额）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/pendingListPage")
    public Result<PageResult<PendingEncounterVO>> pendingListPage(@Valid @RequestBody PendingEncounterPageQueryDTO query) {
        return Result.success(settlementBillService.pendingEncounterPage(query));
    }

    /**
     * 结算账单分页：按患者、账单状态、就诊等条件捞账单头。
     * 用于「结算账单」台账查询卡，以及收费台按就诊列出未收齐账单、退费申请页挑选可退账单的候选取数。
     */
    @Operation(summary = "分页查询结算账单")
    @PreAuthorize("hasAuthority('finance:bill:list')")
    @PostMapping("/listPage")
    public Result<PageResult<BizSettlementBillVO>> listPage(@Valid @RequestBody BillPageQueryDTO query) {
        return Result.success(settlementBillService.selectPage(query));
    }

    /**
     * 账单详情一次给全：账单头、行快照与名下全部收退流水，页面不再自己拼多次请求。
     * 用于账单详情弹框与收款弹框打开时回显尚欠金额，付清与否读后端现算值。
     */
    @Operation(summary = "账单详情（账单行快照 + 全部收/退流水）")
    @PreAuthorize("hasAuthority('finance:bill:list')")
    @GetMapping("/getDetailById")
    public Result<BizSettlementBillDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(settlementBillService.getDetailById(id));
    }

    /**
     * 患者维度的账单行快照：该患者历次账单里已收过费的收费项目，扁平一张列表。
     * 用于医生站开方与今日就诊详情回显「患者已收费项目」，避免重复开单重复收费。
     */
    @Operation(summary = "患者维度账单明细（医生站 / 今日就诊回显「患者已收费项目」）：该患者全部账单行快照")
    // 跨岗位共享参照数据（医生站/今日就诊/收费台均读），按 §8.1 口径统一 isAuthenticated()
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/listItemsByPatient")
    public Result<List<BizSettlementBillItemVO>> listItemsByPatient(@RequestParam Long patientId) {
        return Result.success(settlementBillService.listItemsByPatient(patientId));
    }
}
