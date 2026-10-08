package com.his.miniapp.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 患者端就诊人档案（只读展示，不含扩展明细）。
 */
@Data
public class PatientProfileVO implements Serializable {

    /**
     * 患者ID（字符串化防 BIGINT 精度丢失）
     */
    private String id;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 账户余额（元）
     */
    private BigDecimal balance;

    /**
     * 就诊次数
     */
    private Integer visitCount;
}
