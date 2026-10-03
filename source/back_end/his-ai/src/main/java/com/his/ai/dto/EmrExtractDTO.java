package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 病历文本结构化抽取入参。
 * <p>
 * <b>关于「入参为什么带业务内容」</b>：本项目的审核类接口一律只传业务 ID，
 * 不传业务内容（避免"前端传什么就审什么"的绕过口子）。
 * 本接口是例外，原因是<b>待抽取的文本本身就是输入数据</b>，没有对应的库内实体 ——
 * 医生是从外院系统/上级医院病历/自己的草稿里粘贴进来的。
 * 这里不存在"绕过校验"的风险：本能力不修改任何病历数据，
 * 产出只是一组待医生逐字段采纳的候选值。
 * <p>
 * {@code gender}/{@code age} 只用于消歧（如"男120-160"这类带性别分支的表述），
 * 且<b>仅在 {@code recordId} 缺失时生效</b>：有 recordId 时一律以库内为准。
 */
@Data
@Schema(description = "病历文本结构化抽取入参")
public class EmrExtractDTO {

    @NotBlank(message = "待抽取的文本不能为空")
    @Size(max = 8000, message = "待抽取的文本不能超过 8000 字")
    @Schema(description = "待抽取的自由文本（医生粘贴或口述转写的内容）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String rawText;

    @Schema(description = "病历ID，可选。给了就按库内病历补患者上下文，并作为审计的 bizId")
    private Long recordId;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    @Schema(description = "性别（1-男 2-女），可选；仅在 recordId 缺失时生效")
    private Integer gender;

    /**
     * 年龄
     */
    @Schema(description = "年龄，可选；仅在 recordId 缺失时生效")
    private Integer age;
}
