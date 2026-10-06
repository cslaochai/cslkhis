package com.his.charge.controller;




import com.his.charge.dto.BillPayDTO;
import com.his.charge.dto.BillQueryPageDTO;
import com.his.charge.dto.BillRefundDTO;
import com.his.charge.dto.BillSettleUpsertDTO;
import com.his.charge.dto.BillVoidDTO;
import com.his.charge.dto.PendingEncounterQueryPageDTO;
import com.his.charge.service.PaymentService;
import com.his.charge.service.SettlementBillService;
import com.his.charge.vo.BillPreviewVO;
import com.his.charge.vo.BizPaymentTxnVO;
import com.his.charge.vo.BizSettlementBillDetailVO;
import com.his.charge.vo.BizSettlementBillItemVO;
import com.his.charge.vo.BizSettlementBillVO;
import com.his.charge.vo.PendingEncounterVO;
import com.his.charge.vo.PendingFeeVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 收费结算窗口（L2 结算 + L3 收款/退费）。
 *
 * <p>四步一条链：看未结费用 → 结算出账单 → 收款（可多笔多渠道）→ 需要时按账单退费。
 * 每一步只动自己那一层：出账单不碰钱，收款不碰应收，退费同时冲应收和资金但都在一个事务里。
 */
@Tag(name = "结算账单")
@RestController
@RequestMapping("/charge/settlementBill")
@RequiredArgsConstructor
public class SettlementBillController {

    private final SettlementBillService settlementBillService;
    private final PaymentService paymentService;

    @Operation(summary = "结算台候选：某次就诊下待结算的记账行 + 净额")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @GetMapping("/listPendingFees")
    public Result<PendingFeeVO> listPendingFees(@RequestParam Integer encounterType,
                                                @RequestParam Long encounterId) {
        return Result.success(settlementBillService.pendingFees(encounterType, encounterId));
    }

    @Operation(summary = "出账试算：选中记账行的应收/优惠/医保 split/应缴（不落库）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/settlePreview")
    public Result<BillPreviewVO> settlePreview(@Valid @RequestBody BillSettleUpsertDTO dto) {
        return Result.success(settlementBillService.previewSettlement(dto));
    }

    @Operation(summary = "结算出账：把待结算记账行锁成一张账单")
    @PreAuthorize("hasAuthority('finance:bill:edit')")
    @PostMapping("/settle")
    public Result<BizSettlementBillVO> settle(@Valid @RequestBody BillSettleUpsertDTO dto) {
        return Result.success(settlementBillService.settleVO(dto));
    }

    @Operation(summary = "取消结算（账单作废 + 解锁记账行，已有收款的拒绝）")
    @PreAuthorize("hasAuthority('finance:bill:edit')")
    @PostMapping("/voidBill")
    public Result<Void> voidBill(@Valid @RequestBody BillVoidDTO dto) {
        settlementBillService.voidBill(dto);
        return Result.success(null);
    }

    @Operation(summary = "收款（一次可提交多笔、多渠道）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/pay")
    public Result<List<BizPaymentTxnVO>> pay(@Valid @RequestBody BillPayDTO dto) {
        return Result.success(paymentService.pay(dto));
    }

    @Operation(summary = "按账单退费（红冲记账行 + 逐笔原路退回）")
    @PreAuthorize("hasAuthority('finance:refund:list')")
    @PostMapping("/refund")
    public Result<List<BizPaymentTxnVO>> refund(@Valid @RequestBody BillRefundDTO dto) {
        return Result.success(paymentService.refund(dto));
    }

    @Operation(summary = "收费台首屏：按就诊汇总的待收费榜（待出账应收 + 未收齐账单差额）")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @PostMapping("/pendingListPage")
    public Result<PageResult<PendingEncounterVO>> pendingListPage(@Valid @RequestBody PendingEncounterQueryPageDTO query) {
        return Result.success(settlementBillService.pendingEncounterPage(query));
    }

    @Operation(summary = "分页查询结算账单")
    @PreAuthorize("hasAuthority('finance:bill:list')")
    @PostMapping("/listPage")
    public Result<PageResult<BizSettlementBillVO>> listPage(@Valid @RequestBody BillQueryPageDTO query) {
        return Result.success(settlementBillService.selectPage(query));
    }

    @Operation(summary = "账单详情（账单行快照 + 全部收/退流水）")
    @PreAuthorize("hasAuthority('finance:bill:list')")
    @GetMapping("/getDetailById")
    public Result<BizSettlementBillDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(settlementBillService.getDetailById(id));
    }

    @Operation(summary = "患者维度账单明细（医生站 / 今日就诊回显「患者已收费项目」）：该患者全部账单行快照")
    @PreAuthorize("hasAuthority('finance:cashier:list')")
    @GetMapping("/listItemsByPatient")
    public Result<List<BizSettlementBillItemVO>> listItemsByPatient(@RequestParam Long patientId) {
        return Result.success(settlementBillService.listItemsByPatient(patientId));
    }
}
