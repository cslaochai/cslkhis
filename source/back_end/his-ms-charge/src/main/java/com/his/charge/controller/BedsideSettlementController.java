package com.his.charge.controller;

import com.his.charge.dto.BedsideAdmissionDTO;
import com.his.charge.dto.BedsideBillDTO;
import com.his.charge.dto.BedsidePayNoDTO;
import com.his.charge.dto.BedsideWardBoardQueryDTO;
import com.his.charge.dto.InpatientSettlementUpsertDTO;
import com.his.charge.service.BedsideSettlementService;
import com.his.charge.vo.BedsideSettleResultVO;
import com.his.charge.vo.BedsideWardPatientVO;
import com.his.charge.vo.InpatientSettlementPreviewVO;
import com.his.charge.vo.InpatientSettlementVO;
import com.his.charge.vo.QrPayCreateVO;
import com.his.charge.vo.QrPayResultVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 床旁结算（护士站）：病区护士对分管在院患者办出院结算、亮聚合码。
 *
 * <p>权限码 {@code nur:bedside:settle} 只挂在控制器上（方法级不再细分）：
 * 这个页面的每个动作本来就绑同一次床旁操作，拆成 add/edit 只会让授权表多几行死数据。
 * 真正的边界在病区门禁——服务端按当前岗位科室逐次复核，见
 * {@code BedsideSettlementServiceImpl}。
 */
@Tag(name = "床旁结算（护士站）")
@RestController
@RequestMapping("/charge/bedside")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('nur:bedside:settle')")
public class BedsideSettlementController {

    private final BedsideSettlementService bedsideSettlementService;

    /**
     * 病区在院患者榜（按床号排序，带预交金余额/累计费用/欠费/结算单状态）。
     * 用于床旁结算工作区加载分管病房的患者列表。
     */
    @Operation(summary = "病区在院患者榜（钱况 + 结算状态）")
    @PostMapping("/wardBoard")
    public Result<PageResult<BedsideWardPatientVO>> wardBoard(@Valid @RequestBody BedsideWardBoardQueryDTO q) {
        var page = bedsideSettlementService.wardBoard(q);
        return Result.success(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                page.getRecords()));
    }

    /**
     * 出院结算试算：费用明细 + 医保拆分 + 余额抵扣/退差/欠费结论（不落库）。
     */
    @Operation(summary = "出院结算试算")
    @PostMapping("/settlePreview")
    public Result<InpatientSettlementPreviewVO> settlePreview(@Valid @RequestBody BedsideAdmissionDTO dto) {
        return Result.success(bedsideSettlementService.settlePreview(dto));
    }

    /**
     * 办理出院结算：一次住院一张有效账单，欠费也出账单（留在未付清）。
     * 返回结算单 + 亮码锚点（billId/billNo/未付清额）。
     */
    @Operation(summary = "办理出院结算（带回亮码锚点）")
    @PostMapping("/settle")
    public Result<BedsideSettleResultVO> settle(@Valid @RequestBody InpatientSettlementUpsertDTO dto) {
        return Result.success("结算单已生成", bedsideSettlementService.settle(dto));
    }

    /**
     * 该入院当前有效的出院结算单（已出账回显；没有返回 null）。
     */
    @Operation(summary = "查已出账的结算单")
    @PostMapping("/settlementDetail")
    public Result<InpatientSettlementVO> settlementDetail(@Valid @RequestBody BedsideAdmissionDTO dto) {
        return Result.success(bedsideSettlementService.settlementDetail(dto));
    }

    /**
     * 按账单出聚合支付码（微信/支付宝/云闪通扫）；同账单已有活码直接复用。
     */
    @Operation(summary = "出聚合支付码")
    @PostMapping("/qrCreate")
    public Result<QrPayCreateVO> createQr(@Valid @RequestBody BedsideBillDTO dto) {
        return Result.success(bedsideSettlementService.createQr(dto));
    }

    /**
     * 轮询收款结果：渠道查单兜底入账，返回账单最新未付清额。
     */
    @Operation(summary = "轮询收款结果")
    @PostMapping("/qrQuery")
    public Result<QrPayResultVO> queryQr(@Valid @RequestBody BedsidePayNoDTO dto) {
        return Result.success(bedsideSettlementService.queryQr(dto));
    }

    /**
     * 模拟患者已付款（聚合码模拟模式的验收入口，真模式拒绝）：dev/联调用，
     * 之后轮询即按实收入账。病区门禁同轮询。
     */
    @Operation(summary = "模拟患者已付款（仅模拟模式）")
    @PostMapping("/qrMockPaid")
    public Result<Void> mockPaid(@Valid @RequestBody BedsidePayNoDTO dto,
                                 @RequestParam(required = false) Integer channel) {
        bedsideSettlementService.mockPaid(dto, channel);
        return Result.success(null);
    }

    /**
     * 关掉该账单下全部待支付码（患者不付了/改去收费台时调）。
     */
    @Operation(summary = "关闭该账单全部待支付码")
    @PostMapping("/qrClose")
    public Result<Void> closeQr(@Valid @RequestBody BedsideBillDTO dto) {
        bedsideSettlementService.closeQr(dto);
        return Result.success(null);
    }
}
