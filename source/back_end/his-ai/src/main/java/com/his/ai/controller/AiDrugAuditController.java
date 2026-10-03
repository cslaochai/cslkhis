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
 * <p>
 * 接口只接收处方 ID，不接收处方内容。审核对象由服务端按 ID 回查，
 * 这样「前端传什么就审什么」的绕过路径不存在 —— 审核结论的可信度取决于此。
 */
@Tag(name = "AI 能力-处方合理性审核")
@RestController
@RequestMapping("/ai/drugAudit")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class AiDrugAuditController {

    private final DrugAuditCapability drugAuditCapability;

    @PreAuthorize("hasAuthority('pharmacy:prescriptionAudit:edit')")
    @Operation(summary = "执行处方审核（硬规则 + 大模型长尾，可写入 biz_clinical_rule_check）")
    @PostMapping("/execute")
    public Result<DrugAuditResultVO> execute(@RequestBody @Valid DrugAuditExecuteDTO executeDTO) {
        return Result.success(drugAuditCapability.execute(executeDTO));
    }
}
