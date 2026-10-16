package com.his.patient.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 绑定已有就诊人入参（姓名 + 身份证 + 建档预留手机号上的短信码，三要素四因子校验）。
 */
@Data
public class GuardianBindDTO implements Serializable {

    /**
     * 患者姓名
     */
    @NotBlank(message = "请输入就诊人姓名")
    private String patientName;

    @NotBlank(message = "请输入身份证号")
    private String idCard;

    @NotBlank(message = "请输入短信验证码")
    private String smsCode;

    /**
     * 关系码值（字典患者关系字典），必填
     */
    @NotNull(message = "请选择与就诊人的关系")
    @AllowedValues(values = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "99"}, message = "关系码值不正确")
    private Integer relation;
}
