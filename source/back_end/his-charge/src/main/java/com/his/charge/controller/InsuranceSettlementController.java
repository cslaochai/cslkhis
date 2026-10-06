package com.his.charge.controller;






import com.his.charge.dto.CancelUploadDTO;
import com.his.charge.dto.ReconcileQueryDTO;
import com.his.charge.dto.SettlementAuditDTO;
import com.his.charge.dto.SettlementQueryPageDTO;
import com.his.charge.service.InsuranceChannelService;
import com.his.charge.service.InsuranceSettlementService;
import com.his.charge.vo.BizInsuranceReportVO;
import com.his.charge.vo.BizInsuranceSettlementVO;
import com.his.charge.vo.InsuranceSettlementDetailVO;
import com.his.charge.vo.InsuranceStatsVO;
import com.his.charge.vo.PreSettlementVO;
import com.his.charge.vo.ReconcileResultVO;
import com.his.charge.vo.SettlementResultVO;
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

    private final InsuranceSettlementService settlementService;

    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "分页查询结算清单")
    @GetMapping("/listPage")
    public Result<PageResult<BizInsuranceSettlementVO>> listPage(@Valid SettlementQueryPageDTO queryDTO) {
        return Result.success(settlementService.selectSettlementPage(queryDTO.getPatientId(),
                queryDTO.getPatientName(), queryDTO.getSettlementStatus(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "获取结算清单详情")
    @GetMapping("/getById")
    public Result<BizInsuranceSettlementVO> getById(@RequestParam Long id) {
        return Result.success(settlementService.getSettlementVO(id));
    }

    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "医保工作台统计（今日结算/医保支付/单数 + 待结算/已结算）")
    @GetMapping("/stats")
    public Result<InsuranceStatsVO> stats() {
        return Result.success(settlementService.stats());
    }

    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "获取结算清单完整详情（患者/挂号/病历/账单行）")
    @GetMapping("/getDetailById")
    public Result<InsuranceSettlementDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(settlementService.getSettlementDetailVO(id));
    }

    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "医保预结算")
    @PostMapping("/preSettle")
    public Result<PreSettlementVO> preSettle(@RequestParam Long id) {
        return Result.success(settlementService.preSettlement(id));
    }

    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "正式结算")
    @PostMapping("/settle")
    public Result<SettlementResultVO> settle(@RequestParam Long id) {
        return Result.success(settlementService.settle(id));
    }

    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "报盘上传（G7：组 2304 报文→InsuranceChannelService 发送→回执成功后清单转已上传）")
    @PostMapping("/upload")
    public Result<Void> upload(@RequestParam Long id) {
        boolean success = settlementService.uploadSettlement(id);
        return success ? Result.success("上传成功", null) : Result.error("上传失败");
    }

    @PreAuthorize("hasAuthority('finance:insurance:delete')")
    @Operation(summary = "报盘撤销（2305 报文，回执成功后清单回到已结算）")
    @PostMapping("/cancelUpload")
    public Result<Void> cancelUpload(@Valid @RequestBody CancelUploadDTO actionDTO) {
        boolean success = settlementService.cancelUpload(actionDTO.getId(), actionDTO.getReason());
        return success ? Result.success("撤销成功", null) : Result.error("撤销失败");
    }

    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "报文台账分页（不含报文全文）")
    @GetMapping("/reportListPage")
    public Result<PageResult<BizInsuranceReportVO>> reportListPage(@RequestParam(required = false) Long settlementId,
                                                                   @RequestParam(required = false) Integer reportType,
                                                                   @RequestParam(required = false) Integer status,
                                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(settlementService.selectReportPage(settlementId, reportType, status, pageNum, pageSize));
    }

    @PreAuthorize("hasAuthority('finance:insurance:list')")
    @Operation(summary = "单条报文全文（payload/replyPayload，前端『报盘原文』视图）")
    @GetMapping("/reportById")
    public Result<BizInsuranceReportVO> reportById(@RequestParam Long id) {
        return Result.success(settlementService.getReportDetail(id));
    }

    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "日对账：本地清单 vs 医保侧账单，输出汇总与差异")
    @PostMapping("/reconcile")
    public Result<ReconcileResultVO> reconcile(@Valid @RequestBody ReconcileQueryDTO queryDTO) {
        return Result.success(settlementService.reconcile(queryDTO.getBillDate()));
    }

    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "审核结算清单")
    @PostMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody SettlementAuditDTO actionDTO) {
        boolean success = settlementService.auditSettlement(actionDTO.getId(), actionDTO.getApproved(), actionDTO.getRemark());
        return success ? Result.success("审核成功", null) : Result.error("审核失败");
    }
}
