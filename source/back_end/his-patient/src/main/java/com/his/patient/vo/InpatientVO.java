package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院列表行 VO（自定义 JOIN 查询结果）
 */
@Data
public class InpatientVO {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
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
     * 年龄
     */
    private Integer age;

    /**
     * 医保类型
     */
    private String insuranceType;

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
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /**
     * 床位号
     */
    private String bedNo;

    /**
     * 主治/入院医生姓名
     */
    private String doctorName;

    /**
     * 入院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    /**
     * 入院途径：1-门诊 2-急诊 3-转院 4-其他（可能为 NULL，前端显示"—"而不是默认门诊）
     */
    private Integer admitWay;

    /**
     * 出院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dischargeTime;

    /**
     * 状态：0-已出院 1-在院
     */
    private Integer admitStatus;

    /**
     * 入院诊断（文本）
     */
    private String diagnosis;

    /**
     * 来源挂号号（门诊转住院才有值；直接入院为 NULL，前端显示"—"）
     */
    private String registNo;

    /**
     * 来源住院证号（门诊转住院才有值）
     */
    private String admissionOrderNo;

    /**
     * 病案首页ID（为空说明尚未生成首页）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long summaryId;

    /**
     * 病案首页状态：1-草稿 2-已提交 3-已归档
     */
    private Integer summaryStatus;

    /**
     * 住院天数（在院时为「已住院天数」，出院后为实际上报天数）
     */
    private Integer inpatientDays;

    /**
     * 主要诊断名称（来自首页）
     */
    private String mainDiagnosisName;

    /**
     * 住院总费用
     */
    private BigDecimal totalAmount;
}
