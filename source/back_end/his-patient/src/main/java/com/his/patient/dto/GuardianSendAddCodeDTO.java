package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增建档场景发码入参（码发往该手机号，绑定 addPatient 校验）
 */
@Data
public class GuardianSendAddCodeDTO implements Serializable {

    /** 联系电话 */
    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
