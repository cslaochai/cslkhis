package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 绑定场景发码入参：只按姓名+身份证定位档案，码发往建档预留手机号（不由前端指定）
 */
@Data
public class GuardianSendBindCodeDTO implements Serializable {

    /** 死者姓名 */
    @NotBlank(message = "请输入就诊人姓名")
    private String patientName;

    /** 身份证号 */
    @NotBlank(message = "请输入身份证号")
    private String idCard;
}
