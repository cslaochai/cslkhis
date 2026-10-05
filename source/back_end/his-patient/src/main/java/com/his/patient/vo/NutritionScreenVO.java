package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营养风险筛查记录出参（列表/详情共用）。
 *
 * <p>码值文案全部由 SQL 的 CASE 给出，与 {@code NutritionRules} 逐字对齐；
 * 前端只渲染 {@code xxxText}，不许自己判分数区间。
 */
@Data
public class NutritionScreenVO implements Serializable {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 筛查编号
     */
    private String screenNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 住院号（JOIN 入院记录）
     */
    private String admissionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号（快照）
     */
    private String patientNo;
    /**
     * 患者姓名（快照）
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
     * 科室ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 科室名称（快照）
     */
    private String deptName;

    /**
     * 病区ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;
    /**
     * 病区名称（快照）
     */
    private String wardName;
    /**
     * 床号（快照）
     */
    private String bedNo;

    /**
     * 在院状态：1-在院 0-已出院（来自入院记录）
     */
    private Integer admitStatus;

    /**
     * 量表（1-NRS2002 2-PG-SGA 3-MNA）
     */
    private Integer screenType;
    private String screenTypeText;

    /**
     * NRS2002 营养状态受损评分 0~3（1-体重下降 2-GI手术 3-骨髓移植等）
     */
    private Integer impairScore;
    /**
     * NRS2002 疾病严重程度评分 0~3（1-髋骨骨折 2-腹部大手术 3-颅脑损伤）
     */
    private Integer severityScore;
    /**
     * NRS2002 年龄评分
     */
    private Integer ageScore;

    /**
     * 身高 cm
     */
    private BigDecimal heightCm;
    /**
     * 体重 kg
     */
    private BigDecimal weightKg;
    /**
     * BMI
     */
    private BigDecimal bmi;
    /**
     * 近 3 个月体重下降百分比（%）
     */
    private BigDecimal weightLossPercent;

    /**
     * 量表总分
     */
    private Integer totalScore;

    /**
     * 判定：0-无营养风险 1-有营养风险
     */
    private Integer riskFlag;
    private String riskFlagText;

    /**
     * 筛查时机（1-入院48小时内 2-病情变化复筛 3-术后复筛 4-定期复筛）
     */
    private Integer screenSource;
    private String screenSourceText;

    /**
     * 下次筛查日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextScreenDate;

    /**
     * 复筛是否已到期（下次筛查日期 <= 今天）：SQL 现算，不落状态列
     */
    private Integer reScreenDue;

    /**
     * 分项明细 JSON
     */
    private String itemsJson;

    /**
     * 筛查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime screenTime;

    /**
     * 筛查人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long screenerId;
    /**
     * 筛查人姓名（快照）
     */
    private String screenerName;

    /**
     * 备注
     */
    private String remark;
}
