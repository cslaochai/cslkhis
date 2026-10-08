package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 病历文本结构化抽取入参。
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
