package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 危急值处置入参
 */
@Data
public class CriticalValueHandleDTO {

    /**
     * 危急值ID
     */
    @NotNull(message = "危急值ID不能为空")
    private Long criticalValueId;

    /**
     * 处置措施。危急值必须留下处置记录，因此不允许为空 ——
     * 「已处置」但没有措施，等于闭环是假闭合。
     */
    @NotBlank(message = "处置措施不能为空")
    private String handleMeasure;

    /**
     * 处置人；为空时取当前登录账号
     */
    private String handleBy;
}
