package com.his.miniapp.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 患者主档行（患者端就诊人档案页），对应 {@code MiniappDirectoryMapper#selectPatientById}。
 *
 * <p>本 VO 是 Mapper 的行承载，字段名与出参 {@link PatientDetailVO} 完全同形 ——
 * 两者的差别只在 {@code id}：本类的 {@code id} 是 SQL {@code CAST(id AS CHAR)} 出来的字符串
 * （Mapper 行不经 {@code ToStringSerializer}，不 CAST 会在 JS 端丢精度）。
 */
@Data
public class PatientRowVO implements Serializable {

    /**
     * 患者ID（SQL 已 CAST 成字符串，防 BIGINT 精度丢失）
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
