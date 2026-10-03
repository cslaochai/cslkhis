package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编码任务提交入参（编码员提交 ICD 编码）
 */
@Data
public class CodeTaskSubmitDTO {

    /** 任务ID */
    @NotNull(message = "任务ID不能为空")
    private Long id;

    /** 主诊断 ICD-10 编码 */
    @NotBlank(message = "主诊断 ICD 编码不能为空")
    private String mainIcdCode;

    /** 主诊断名称 */
    @NotBlank(message = "主诊断名称不能为空")
    private String mainIcdName;

    /** 其他诊断/手术 ICD（文本，可空） */
    private String otherIcdText;
}
