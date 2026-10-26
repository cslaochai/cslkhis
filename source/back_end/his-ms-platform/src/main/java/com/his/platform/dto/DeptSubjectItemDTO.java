package com.his.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 单个「科室 × 科目」映射项。
 */
@Data
public class DeptSubjectItemDTO {

    /**
     * 诊疗科目代码
     */
    @NotBlank(message = "诊疗科目代码不能为空")
    private String subjectCode;

    /**
     * 是否该科室的上报主科目：1-是 0-否。同一科室只允许一条为 1，服务端会保证唯一。
     */
    private Integer isPrimary;
}
