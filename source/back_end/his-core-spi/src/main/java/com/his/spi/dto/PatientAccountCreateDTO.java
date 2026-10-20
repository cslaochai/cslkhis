package com.his.spi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 患者登录账号开通入参：小程序自助建档时随患者主档一起创建的患者类型账号。
 *
 * <p>账号写权归 his-system，患者域只提交开户申请；
 * 参数合法性（账号唯一、口令强度）由实现方在落库前判定。
 */
@Data
@NoArgsConstructor
public class PatientAccountCreateDTO implements Serializable {

    /**
     * 登录账号名（自助注册用手机号）
     */
    private String userName;

    /**
     * 登录口令明文：仅在进程内传递给实现方加密落库，调用方不得留存
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 绑定的患者ID
     */
    private Long patientId;
}
