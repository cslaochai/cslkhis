package com.his.ai.controller;

import com.his.ai.dto.EmergencyTriageDTO;
import com.his.ai.service.EmergencyTriageCapability;
import com.his.ai.vo.EmergencyTriageResultVO;
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
 * AI 能力 - 急诊分诊建议（P1-2）
 * <p>
 * 刻意只有一个「建议」接口，且<b>没有写回接口</b> ——
 * 分诊级别始终由分诊护士在原有界面上确认后保存。
 */
@Tag(name = "AI能力-急诊分诊")
@RestController
@RequestMapping("/ai/emergencyTriage")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:emergency:list')")
public class AiEmergencyController {

    private final EmergencyTriageCapability emergencyTriageCapability;

    @Operation(summary = "急诊分诊建议（只升不降，仅供护士确认）")
    @PostMapping("/suggest")
    public Result<EmergencyTriageResultVO> suggest(@Valid @RequestBody EmergencyTriageDTO dto) {
        return Result.success(emergencyTriageCapability.suggest(dto));
    }
}
