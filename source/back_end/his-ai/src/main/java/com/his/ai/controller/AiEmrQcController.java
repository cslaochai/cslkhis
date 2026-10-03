package com.his.ai.controller;

import com.his.ai.dto.EmrQcExecuteDTO;
import com.his.ai.service.EmrQcCapability;
import com.his.ai.vo.EmrQcResultVO;
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
 * 病历内涵质控接口。
 * <p>
 * 与既有的 {@code /charge/qualityControl/executeQc} 关系同 ICD：并存而非替换。
 * 既有接口的 {@code executeQc} 只落一条「默认通过」的记录，没有实质检查逻辑；
 * 本接口补齐实质内容，并把结论写回同一份质控检查记录，复用已有列表页。
 */
@Tag(name = "AI 能力-病历内涵质控")
@RestController
@RequestMapping("/ai/emrQc")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AiEmrQcController {

    private final EmrQcCapability emrQcCapability;

    @PreAuthorize("hasAuthority('qc:recordQc:edit')")
    @Operation(summary = "执行病历内涵质控（必填项规则 + 大模型内涵审查，可写入 biz_quality_control）")
    @PostMapping("/execute")
    public Result<EmrQcResultVO> execute(@RequestBody @Valid EmrQcExecuteDTO executeDTO) {
        return Result.success(emrQcCapability.execute(executeDTO));
    }
}
