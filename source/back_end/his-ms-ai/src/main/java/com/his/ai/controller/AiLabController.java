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

    /**
     * 解读一份检验报告：异常项、危急值与升降趋势由代码判定，组合异常时才由模型做连贯解读。
     * 用于检验结果详情弹框发起智能解读，勾选后结论与建议可写回检验记录。
     */
    @PreAuthorize("hasAuthority('medtech:laboratoryWorkstation:edit')")
    @Operation(summary = "检验结果解读（异常项 + 趋势 + 结论草稿）")
    @PostMapping("/execute")
    public Result<LabInterpretResultVO> execute(@Valid @RequestBody LabInterpretExecuteDTO dto) {
        return Result.success(labInterpretCapability.execute(dto));
    }
}
