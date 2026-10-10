package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 患者自助注册入参（小程序端患者建档 + 开通登录账号）
 */
@Data
public class PatientRegisterDTO implements Serializable {

    /**
     * 患者姓名
     */
    @NotBlank(message = "请输入姓名")
    private String patientName;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 身份证号（建档与唯一性判定，格式校验在 service 内执行）
     */
    private String idCard;

    /**
     * 手机号（同时作为登录账号）
     */
    private String phone;

    /**
     * 登录密码：SM2 公钥加密后的十六进制密文（C1C3C2，不带 04 前缀）。
     *
     * <p>公钥从 {@code GET /auth/publicKey} 取，与登录共用一对密钥；明文一律拒收。
     */
    private String password;

    /**
     * 手机短信验证码（scene=register，先校验通过才建档）
     */
    private String smsCode;
}
