package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 答卷作废入参（填错/重复时用它，而不是删除）。
 */
@Data
public class SurveyAnswerVoidDTO implements Serializable {

    @NotNull(message = "答卷ID不能为空")
    private Long id;

    /**
     * 原因
     */
    @NotBlank(message = "作废原因不能为空")
    private String reason;
}
