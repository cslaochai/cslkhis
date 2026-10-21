package com.his.ai.controller;

import com.his.ai.dto.InsuranceEvidenceDTO;
import com.his.ai.service.InsuranceEvidenceCapability;
import com.his.ai.vo.InsuranceEvidenceVO;
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
 * 医保审核证据判定
 */
@Tag(name = "医保审核证据判定")
@RestController
@RequestMapping("/ai/insurance")
@RequiredArgsConstructor
public class AiInsuranceController {

    private final InsuranceEvidenceCapability insuranceEvidenceCapability;

    /**
     * 对一条合规审核记录的命中项逐条判定证据支持、证据反驳还是证据不足，只读计算不改规则结论。
     * 用于医保合规审核详情弹框提示人工复核；模型未产出判定时前端必须显示警示而非「无风险」。
     */
    @Operation(summary = "对一条合规审核记录做证据判定（supported/refuted/insufficient，提示人工复核）")
    @PreAuthorize("hasAuthority('finance:complianceAudit:list')")
    @PostMapping("/evidence")
    public Result<InsuranceEvidenceVO> evidence(@Valid @RequestBody InsuranceEvidenceDTO dto) {
        return Result.success(insuranceEvidenceCapability.execute(dto));
    }
}
