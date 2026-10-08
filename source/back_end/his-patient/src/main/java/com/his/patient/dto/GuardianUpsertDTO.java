package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增就诊人入参（建档并自动绑定当前账号，不建登录账号）。
 */
@Data
public class GuardianUpsertDTO implements Serializable {

    /** 患者姓名 */
    @NotBlank(message = "请输入就诊人姓名")
    private String patientName;

    /** 性别（1-男 2-女 9-未知） */
    @NotNull(message = "请选择性别")
    private Integer gender;

    /** 身份证号（必填，出生日期/年龄由此解析；已建档则提示改用绑定） */
    @NotBlank(message = "请输入身份证号")
    private String idCard;

    /** 手机号码 */
    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "请输入短信验证码")
    private String smsCode;

    /** 关系码值（字典患者关系字典），必填 */
    @NotNull(message = "请选择与就诊人的关系")
    private Integer relation;
}
