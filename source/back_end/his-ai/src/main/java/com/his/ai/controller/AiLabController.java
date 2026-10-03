package com.his.ai.controller;

import com.his.ai.dto.LabInterpretExecuteDTO;
import com.his.ai.service.LabInterpretCapability;
import com.his.ai.vo.LabInterpretResultVO;
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
 * AI 能力 - 检验结果解读（P1-1）
 */
@Tag(name = "AI能力-检验解读")
@RestController
@RequestMapping("/ai/labInterpret")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:list')")
public class AiLabController {

    private final LabInterpretCapability labInterpretCapability;

    @PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:edit')")
    @Operation(summary = "检验结果解读（异常项 + 趋势 + 结论草稿）")
    @PostMapping("/execute")
    public Result<LabInterpretResultVO> execute(@Valid @RequestBody LabInterpretExecuteDTO dto) {
        return Result.success(labInterpretCapability.execute(dto));
    }
}
