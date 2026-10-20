package com.his.charge.controller;


import com.his.charge.dto.ComplianceAuditPageQueryDTO;
import com.his.charge.dto.ComplianceBatchAuditDTO;
import com.his.charge.dto.SettlementCodingUpsertDTO;
import com.his.charge.service.ComplianceAuditService;
import com.his.charge.vo.ComplianceAuditDetailVO;
import com.his.charge.vo.ComplianceAuditVO;
import com.his.charge.vo.SettlementCodingVO;
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
 * 医保合规审核控制器（防止高编高套 / 低编入组）
 */
@Tag(name = "医保合规审核（高编高套与低编入组）")
@RestController
@RequestMapping("/charge/compliance")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('finance:insurance:list')")
public class ComplianceAuditController {

    private final ComplianceAuditService complianceAuditService;

    /**
     * 取指定结算清单已录入的诊断与手术操作编码明细。
     * 用于医保结算页的清单编码录入与合规自查前置展示。
     */
    @Operation(summary = "查询结算清单编码明细（诊断+手术操作）")
    @GetMapping("/coding")
    public Result<SettlementCodingVO> coding(@RequestParam Long settlementId) {
        return Result.success(complianceAuditService.getCoding(settlementId));
    }

    /**
     * 整单覆盖保存结算清单的编码明细。
     * 用于医保编码录入环节修正诊断/手术编码后回写，作为高编高套自查的输入。
     */
    @PreAuthorize("hasAuthority('finance:insurance:add')")
    @Operation(summary = "保存结算清单编码明细（整单覆盖）")
    @PostMapping("/coding")
    public Result<Void> saveCoding(@Valid @RequestBody SettlementCodingUpsertDTO dto) {
        complianceAuditService.saveCoding(dto);
        return Result.success("保存成功", null);
    }

    /**
     * 清空指定结算清单已录入的编码明细。
     * 用于编码录错需要推倒重录的环节。
     */
    @PreAuthorize("hasAuthority('finance:insurance:delete')")
    @Operation(summary = "清空结算清单编码明细")
    @DeleteMapping("/coding")
    public Result<Void> clearCoding(@RequestParam Long settlementId) {
        complianceAuditService.clearCoding(settlementId);
        return Result.success("已清空", null);
    }

    /**
     * 对单张结算清单做合规自查，输出命中规则与结论。
     * 用于医保结算页提交前拦截高编高套、低编入组。
     */
    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "单张清单合规自查")
    @PostMapping("/audit")
    public Result<ComplianceAuditDetailVO> audit(@RequestParam Long settlementId,
                                                 @RequestParam(required = false) Integer auditType) {
        return Result.success(complianceAuditService.audit(settlementId, auditType));
    }

    /**
     * 按筛选条件批量筛查结算清单的合规问题。
     * 用于合规审核环节对一段时间内的清单集中排查，逐单给出命中明细。
     */
    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "批量合规筛查")
    @PostMapping("/batchAudit")
    public Result<List<ComplianceAuditDetailVO>> batchAudit(@Valid @RequestBody ComplianceBatchAuditDTO dto) {
        return Result.success(complianceAuditService.batchAudit(dto));
    }

    /**
     * 分页查询历史合规审核记录台账。
     * 用于合规审核台账页按患者、时间等条件回看已筛查结果。
     */
    @Operation(summary = "分页查询合规审核记录（菜单 1012 /compliance-audit 台账入口）")
    @PreAuthorize("hasAuthority('finance:complianceAudit:list')")
    @GetMapping("/listPage")
    public Result<PageResult<ComplianceAuditVO>> listPage(@Valid ComplianceAuditPageQueryDTO queryDTO) {
        return Result.success(complianceAuditService.selectAuditPage(queryDTO));
    }

    /**
     * 取单条合规审核记录的详情：主单、命中规则与诊断/手术依据核对。
     * 用于合规审核台账页点开某条记录查看完整判定过程。
     */
    @Operation(summary = "查询合规审核详情（主表 + 命中规则明细 + 诊断/手术依据核对）")
    @PreAuthorize("hasAuthority('finance:complianceAudit:list')")
    @GetMapping("/detail")
    public Result<ComplianceAuditDetailVO> detail(@RequestParam Long auditId) {
        return Result.success(complianceAuditService.getAuditDetail(auditId));
    }
}
