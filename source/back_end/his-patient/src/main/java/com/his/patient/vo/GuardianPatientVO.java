package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 小程序「我的就诊人」列表出参
 */
@Data
public class GuardianPatientVO implements Serializable {

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 性别（1-男 2-女 9-未知） */
    private Integer gender;

    /** 年龄 */
    private Integer age;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** 手机号（服务端已打码，如 138****8888） */
    private String phone;

    /** 身份证号（服务端已打码，前6后4，出网不携带明文） */
    private String idCard;

    /** 关系码值（字典患者关系字典） */
    private Integer relation;

    private String relationText;

    /** 是否默认就诊人（0-否 1-是） */
    private Integer isDefault;

    /** 是否账号本人（用户的患者ID 指向的档案） */
    private Boolean self;
}
