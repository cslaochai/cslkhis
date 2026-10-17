package com.his.ai.controller;

import com.his.ai.service.DeteriorationAlertCapability;
import com.his.ai.vo.DeteriorationExplainVO;
import com.his.ai.vo.DeteriorationScanVO;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 危重预警·病情恶化评分（G-12）。
 */
@Tag(name = "危重预警·病情恶化评分")
@RestController
@RequestMapping("/ai/deterioration")
@RequiredArgsConstructor
public class AiDeteriorationController {

    private final DeteriorationAlertCapability deteriorationAlertCapability;

    @Operation(summary = "病区扫描：在院患者 MEWS+SpO2 评分（纯代码，无模型调用）")
    @PreAuthorize("hasAnyAuthority('ipd:nurse:list','ipd:bedCenter:list')")
    @GetMapping("/wardScan")
    public Result<List<DeteriorationScanVO>> wardScan(@RequestParam Long wardId) {
        return Result.success(deteriorationAlertCapability.wardScan(wardId));
    }

    @Operation(summary = "单患者评分明细；达预警阈值时附模型观察建议（degraded 必显）")
    @PreAuthorize("hasAnyAuthority('ipd:nurse:list','ipd:bedCenter:list')")
    @GetMapping("/explain")
    public Result<DeteriorationExplainVO> explain(@RequestParam Long admissionId) {
        return Result.success(deteriorationAlertCapability.explain(admissionId));
    }
}
