package com.his.charge.controller;


import com.his.charge.dto.CancelUploadDTO;
import com.his.charge.dto.ReconcileQueryDTO;
import com.his.charge.dto.SettlementAuditDTO;
import com.his.charge.dto.SettlementPageQueryDTO;
import com.his.charge.service.InsuranceSettlementService;
import com.his.charge.vo.*;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 医保结算管理控制器
 */
@Tag(name = "医保结算管理")
@RestController
@RequestMapping("/charge/settlement")
@RequiredArgsConstructor
public class InsuranceSettlementController {

    private final InsuranceSettlementService insuranceSettlementService;

    /**
     * 分页查询医保结算清单（L2 医保 split 与结算状态）。
     * 用于医保结算管理页按患者、患者名、结算状态检索清单。
     */
    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "分页查询结算清单")
    @GetMapping("/listPage")
    public Result<PageResult<BizInsuranceSettlementVO>> listPage(@Valid SettlementPageQueryDTO queryDTO) {
        return Result.success(insuranceSettlementService.selectSettlementPage(queryDTO.getPatientId(),
                queryDTO.getPatientName(), queryDTO.getSettlementStatus(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 取单条医保结算清单基本信息。
     * 用于医保结算管理页列表行的详情回显。
     */
    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "获取结算清单详情")
    @GetMapping("/getById")
    public Result<BizInsuranceSettlementVO> getById(@RequestParam Long id) {
        return Result.success(insuranceSettlementService.getSettlementVO(id));
    }

    /**
     * 医保工作台汇总：今日结算数、医保支付额与单数、待结算/已结算计数。
     * 用于医保结算管理页顶部统计卡。
     */
    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "医保工作台统计（今日结算/医保支付/单数 + 待结算/已结算）")
    @GetMapping("/stats")
    public Result<InsuranceStatsVO> stats() {
        return Result.success(insuranceSettlementService.stats());
    }

    /**
     * 取结算清单完整详情：患者、挂号、病历与账单行拼合的全景视图。
     * 用于医保结算页点开某条清单核对结算依据。
     */
    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "获取结算清单完整详情（患者/挂号/病历/账单行）")
    @GetMapping("/getDetailById")
    public Result<InsuranceSettlementDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(insuranceSettlementService.getSettlementDetailVO(id));
    }

    /**
     * 对清单发起医保预结算，试算统筹、个账与自付 split，不落正式结算。
     * 用于医保结算环节正式结算前的费用试算确认。
     */
    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "医保预结算")
    @PostMapping("/preSettle")
    public Result<PreSettlementVO> preSettle(@RequestParam Long id) {
        return Result.success(insuranceSettlementService.preSettlement(id));
    }

    /**
     * 对清单执行正式医保结算，确认统筹与个账支付并推进清单到已结算。
     * 用于医保结算环节预结算核对无误后的正式结算动作。
     */
    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "正式结算")
    @PostMapping("/settle")
    public Result<SettlementResultVO> settle(@RequestParam Long id) {
        return Result.success(insuranceSettlementService.settle(id));
    }

    /**
     * 组医保报盘报文上传至医保渠道，回执成功后清单置为已上传（报盘动作）。
     * 用于结算完成后向医保局报送费用的环节。
     */
    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "报盘上传（G7：组 2304 报文→InsuranceChannelService 发送→回执成功后清单转已上传）")
    @PostMapping("/upload")
    public Result<Void> upload(@RequestParam Long id) {
        boolean success = insuranceSettlementService.uploadSettlement(id);
        return success ? Result.success("上传成功", null) : Result.error("上传失败");
    }

    /**
     * 撤销已上传的报盘，回执成功后清单回退到已结算（报盘撤销动作）。
     * 用于报盘上传有误需要撤回重报的环节。
     */
    @PreAuthorize("hasAuthority('finance:insurance:delete')")
    @Operation(summary = "报盘撤销（2305 报文，回执成功后清单回到已结算）")
    @PostMapping("/cancelUpload")
    public Result<Void> cancelUpload(@Valid @RequestBody CancelUploadDTO actionDTO) {
        boolean success = insuranceSettlementService.cancelUpload(actionDTO.getId(), actionDTO.getReason());
        return success ? Result.success("撤销成功", null) : Result.error("撤销失败");
    }

    /**
     * 分页查询报盘报文台账，不含报文全文。
     * 用于医保结算页查看历次报文交互记录。
     */
    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "报文台账分页（不含报文全文）")
    @GetMapping("/reportListPage")
    public Result<PageResult<BizInsuranceReportVO>> reportListPage(@RequestParam(required = false) Long settlementId,
                                                                   @RequestParam(required = false) Integer reportType,
                                                                   @RequestParam(required = false) Integer status,
                                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(insuranceSettlementService.selectReportPage(settlementId, reportType, status, pageNum, pageSize));
    }

    /**
     * 取单条报盘的报文与回执全文。
     * 用于报文台账点开某条查看报盘原文，仅供排障不回喂列表。
     */
    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "单条报文全文（payload/replyPayload，前端『报盘原文』视图）")
    @GetMapping("/reportById")
    public Result<BizInsuranceReportVO> reportById(@RequestParam Long id) {
        return Result.success(insuranceSettlementService.getReportDetail(id));
    }

    /**
     * 按账单日做日对账：本地清单与医保侧账单比对，输出汇总与差异（对账动作）。
     * 用于医保结算页核对账实是否相符、定位长短款。
     */
    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "日对账：本地清单 vs 医保侧账单，输出汇总与差异")
    @PostMapping("/reconcile")
    public Result<ReconcileResultVO> reconcile(@Valid @RequestBody ReconcileQueryDTO queryDTO) {
        return Result.success(insuranceSettlementService.reconcile(queryDTO.getBillDate()));
    }

    /**
     * 审核医保结算清单，通过或驳回并记录审核意见。
     * 用于医保结算环节的清单复核，决定能否进入报盘。
     */
    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "审核结算清单")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody SettlementAuditDTO actionDTO) {
        boolean success = insuranceSettlementService.auditSettlement(actionDTO.getId(), actionDTO.getApproved(), actionDTO.getRemark());
        return success ? Result.success("审核成功", null) : Result.error("审核失败");
    }
}
