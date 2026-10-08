package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预防措施记录
 */
@Data
public class VtePreventVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 措施记录编号
     */
    private String preventNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床号
     */
    private String bedNo;

    /**
     * 来源评估单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assessmentId;

    /**
     * Caprini 总分
     */
    private Integer capriniScore;

    /**
     * 风险等级（1-低 2-中 3-高 4-极高）
     */
    private Integer riskLevel;

    private String riskLevelText;

    /**
     * 措施码
     */
    private String measureCode;

    private String measureCodeText;

    /**
     * 措施类别（1-基础预防 2-物理预防 3-药物预防）
     */
    private Integer measureType;

    private String measureTypeText;

    /**
     * 措施名称
     */
    private String measureName;

    /**
     * 计划执行日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    /**
     * 落实状态（0-待落实 1-已落实 2-禁忌未用 3-患者拒绝）
     */
    private Integer executeStatus;

    private String executeStatusText;

    /**
     * 落实时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;

    /**
     * 执行人姓名
     */
    private String executorName;

    /**
     * 未落实原因
     */
    private String reason;

    /**
     * 备注
     */
    private String remark;
}
