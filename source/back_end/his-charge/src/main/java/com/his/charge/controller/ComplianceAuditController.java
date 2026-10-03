package com.his.charge.controller;

import com.his.charge.dto.ComplianceAuditQueryPageDTO;
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
 *
 * <p>接口分两层：</p>
 * <ol>
 *   <li><b>编码明细维护</b>（/coding）：把结算清单从「一个诊断字符串」升级成
 *       「主诊断+其他诊断+ICD编码+入院病情+CC/MCC+手术操作」的结构化明细。
 *       没有这一层，审核无从谈起 —— 这也是接入医保局的前置条件。</li>
 *   <li><b>合规审核</b>（/audit、/batchAudit、/listPage、/detail）：跑规则、出三态结论、留痕。</li>
 * </ol>
 */
@Tag(name = "医保合规审核（高编高套与低编入组）")
@RestController
@RequestMapping("/charge/compliance")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('finance:insurance:list')")
public class ComplianceAuditController {

    private final ComplianceAuditService complianceAuditService;

    @Operation(summary = "查询结算清单编码明细（诊断+手术操作）")
    @GetMapping("/coding")
    public Result<SettlementCodingVO> coding(@RequestParam Long settlementId) {
        return Result.success(complianceAuditService.getCoding(settlementId));
    }

    @PreAuthorize("hasAuthority('finance:insurance:add')")
    @Operation(summary = "保存结算清单编码明细（整单覆盖）")
    @PostMapping("/coding")
    public Result<Void> saveCoding(@Valid @RequestBody SettlementCodingUpsertDTO dto) {
        complianceAuditService.saveCoding(dto);
        return Result.success("保存成功", null);
    }

    @PreAuthorize("hasAuthority('finance:insurance:delete')")
    @Operation(summary = "清空结算清单编码明细")
    @DeleteMapping("/coding")
    public Result<Void> clearCoding(@RequestParam Long settlementId) {
        complianceAuditService.clearCoding(settlementId);
        return Result.success("已清空", null);
    }

    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "单张清单合规自查")
    @PostMapping("/audit")
    public Result<ComplianceAuditDetailVO> audit(@RequestParam Long settlementId,
                                                 @RequestParam(required = false) Integer auditType) {
        return Result.success(complianceAuditService.audit(settlementId, auditType));
    }

    @PreAuthorize("hasAuthority('finance:insurance:edit')")
    @Operation(summary = "批量合规筛查")
    @PostMapping("/batchAudit")
    public Result<List<ComplianceAuditDetailVO>> batchAudit(@Valid @RequestBody ComplianceBatchAuditDTO dto) {
        return Result.success(complianceAuditService.batchAudit(dto));
    }

    @Operation(summary = "分页查询合规审核记录（菜单 1012 /compliance-audit 台账入口）")
    @PreAuthorize("hasAuthority('finance:complianceAudit:list')")
    @GetMapping("/listPage")
    public Result<PageResult<ComplianceAuditVO>> listPage(@Valid ComplianceAuditQueryPageDTO queryDTO) {
        return Result.success(complianceAuditService.selectAuditPage(queryDTO));
    }

    @Operation(summary = "查询合规审核详情（主表 + 命中规则明细 + 诊断/手术依据核对）")
    @PreAuthorize("hasAuthority('finance:complianceAudit:list')")
    @GetMapping("/detail")
    public Result<ComplianceAuditDetailVO> detail(@RequestParam Long auditId) {
        return Result.success(complianceAuditService.getAuditDetail(auditId));
    }
}
