package com.his.ai.controller;

import com.his.ai.dto.DrugAuditExecuteDTO;
import com.his.ai.service.DrugAuditCapability;
import com.his.ai.vo.DrugAuditResultVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处方合理性审核接口。
 */
@Tag(name = "AI 能力-处方合理性审核")
@RestController
@RequestMapping("/ai/drugAudit")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class AiDrugAuditController {

    private final DrugAuditCapability drugAuditCapability;

    /**
     * 审核一张已落库处方：先跑院内硬规则，再由大模型补长尾合理性判断，可选择把结论写成规则核查记录。
     * 用于处方审核环节出具结论；只按处方主键由服务端回查明细，不接受前端直传处方内容。
     */
    @PreAuthorize("hasAuthority('pharmacy:prescriptionAudit:edit')")
    @Operation(summary = "执行处方审核（硬规则 + 大模型长尾，可写入 biz_clinical_rule_check）")
    @PostMapping("/execute")
    public Result<DrugAuditResultVO> execute(@RequestBody @Valid DrugAuditExecuteDTO executeDTO) {
        return Result.success(drugAuditCapability.execute(executeDTO));
    }
}
