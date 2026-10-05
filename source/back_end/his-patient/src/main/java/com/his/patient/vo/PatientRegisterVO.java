package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 患者自助注册出参
 */
@Data
public class PatientRegisterVO implements Serializable {

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 患者档案号
     */
    private String patientNo;

    /**
     * 登录账号（手机号）
     */
    private String username;
}
