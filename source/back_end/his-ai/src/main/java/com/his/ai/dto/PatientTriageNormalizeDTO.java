package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 患者端导诊口语归一入参。
 *
 * <p>入参只有患者自己打的一句话，不收任何身份参数 —— 归一结果不会落到库里，
 * 只是当次请求的一次文本整理，身份在这里没有用途。
 */
@Data
@Schema(description = "患者端导诊口语归一入参")
public class PatientTriageNormalizeDTO {

    @NotBlank(message = "description不能为空")
    @Schema(description = "患者原话主诉", example = "这两天脑袋昏昏沉沉的还想吐")
    private String description;
}
