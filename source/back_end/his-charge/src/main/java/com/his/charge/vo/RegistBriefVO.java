package com.his.charge.vo;

import com.his.charge.api.AppointGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 挂号/就诊记录跨域摘要：收费域只读预约挂号信息的最小字段集。
 *
 * <p>结算证据、医保结算清单、合规稽核都要引用挂号信息（就诊类型、就诊日期、
 * 科室、医生、医保身份），但不该看见 {@code biz_appoint_info} 的全宽实体 ——
 * 映射由 his-appoint 在实现端口时完成。
 *
 * @see AppointGateway
 */
@Data
@NoArgsConstructor
public class RegistBriefVO {

    /**
     * 挂号ID（biz_appoint_info 主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 挂号单号（收费明细来源单号，科室反查用）
     */
    private String registNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（挂号时点的姓名快照）
     */
    private String patientName;

    /**
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄（岁）
     */
    private Integer age;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 就诊类型（1-初诊 2-复诊；<b>与挂号类型 regist_type 不是一回事</b>）
     */
    private Integer visitType;

    /**
     * 就诊日期（当日就诊范围判定用它，不用 regist_time）
     */
    private LocalDate visitDate;

    /**
     * 医保类型
     */
    private String medicalInsuranceType;

    /**
     * 医保卡号/医保编号
     */
    private String medicalInsuranceNo;
}
