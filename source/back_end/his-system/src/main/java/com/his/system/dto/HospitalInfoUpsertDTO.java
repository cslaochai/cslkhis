package com.his.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 医院基础信息配置 upsert DTO
 */
@Data
public class HospitalInfoUpsertDTO {

    @NotBlank(message = "医院名称不能为空")
    private String hospitalName;

    @NotBlank(message = "医院地址不能为空")
    private String hospitalAddress;

    @NotBlank(message = "联系电话不能为空")
    private String hospitalPhone;

    private String hospitalEmail;
}
