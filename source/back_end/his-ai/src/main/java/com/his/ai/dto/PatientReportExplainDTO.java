package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 患者端报告解读入参。
 * <p>
 * 入参只有报告 ID，没有 patientId —— 患者只能解读<b>自己的</b>报告，
 * 身份一律服务端从登录态取，前端传什么都不作数。
 * 这不是偷懒：一旦允许前端传 patientId，改个数字就能读别人的检验结果。
 */
@Data
@Schema(description = "患者端报告解读入参")
public class PatientReportExplainDTO {

    /**
     * 报告ID 按字符串收：雪花 ID 超过 2^53，前端按 JSON 数字传会丢精度，
     * 现象是「点了报告却说不存在」而且不报错（踩过多次）。
     */
    @NotBlank(message = "reportId不能为空")
    @Schema(description = "报告ID", example = "1")
    private String reportId;
}
