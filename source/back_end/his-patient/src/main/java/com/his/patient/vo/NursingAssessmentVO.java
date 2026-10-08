package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 护理评估单出参。
 */
@Data
public class NursingAssessmentVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 评估单号 AS+yyyyMMdd+4位
     */
    private String assessNo;

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
     * 患者编号
     */
    private String patientNo;
    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 病区名称
     */
    private String wardName;
    /**
     * 床号
     */
    private String bedNo;

    /**
     * 评估类型（1-压疮Braden 2-跌倒Morse 3-疼痛NRS）
     */
    private Integer assessType;
    private String assessTypeText;

    /**
     * 总分
     */
    private Integer totalScore;
    /**
     * 风险等级（1-低风险 2-中风险 3-高风险 4-极高风险）
     */
    private Integer riskLevel;
    private String riskLevelText;

    /**
     * 评分明细 JSON（原样给前端渲染量表）
     */
    private String itemsJson;

    /**
     * 评估时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assessTime;

    /**
     * 评估护士ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assessNurseId;

    /**
     * 评估护士姓名
     */
    private String assessNurseName;

    /**
     * 备注
     */
    private String remark;
}
