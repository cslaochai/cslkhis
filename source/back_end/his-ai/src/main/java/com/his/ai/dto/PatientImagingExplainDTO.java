package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 患者端影像报告解读入参。
 * <p>
 * 与检验解读同口径：入参只有报告 ID，患者身份一律服务端从登录态取，
 * 前端传 patientId 不作数——改个数字就能读别人的报告。
 */
@Data
@Schema(description = "患者端影像报告解读入参")
public class PatientImagingExplainDTO {

    /**
     * 报告ID 按字符串收：雪花 ID 超过 2^53，前端按 JSON 数字传会丢精度。
     */
    @NotBlank(message = "reportId不能为空")
    @Schema(description = "报告ID", example = "1")
    private String reportId;
}
