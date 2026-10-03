package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 慢病建档入参
 */
@Data
public class ChronicUpsertDTO {
    @NotNull(message = "患者ID不能为空")
    private Long patientId;
    @NotBlank(message = "患者号不能为空")
    private String patientNo;
    @NotBlank(message = "患者姓名不能为空")
    private String patientName;
    @NotBlank(message = "慢病编码不能为空")
    private String diseaseCode;
    @NotBlank(message = "慢病名称不能为空")
    private String diseaseName;
    private String remark;
}
