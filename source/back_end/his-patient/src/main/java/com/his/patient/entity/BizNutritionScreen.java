package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营养风险筛查与评定记录（sql/168 §1）。
 */
@Data
@TableName("biz_nutrition_screen")
public class BizNutritionScreen {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 筛查编号（NS+yyyyMMdd+4位序号）
     */
    private String screenNo;

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
     * 床号
     */
    private String bedNo;

    /**
     * 量表（1-NRS2002 2-PG-SGA 3-MNA）
     */
    private Integer screenType;
    /**
     * NRS2002 营养状态受损评分 0~3（1-体重下降 2-GI手术 3-骨髓移植等）
     */
    private Integer impairScore;
    /**
     * NRS2002 疾病严重程度评分 0~3（1-髋骨骨折 2-腹部大手术 3-颅脑损伤）
     */
    private Integer severityScore;
    /**
     * 年龄评分（≥70 岁加 1 分）
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
     * BMI（服务端算）
     */
    private BigDecimal bmi;
    /**
     * 近 3 个月体重下降百分比（%）
     */
    private BigDecimal weightLossPercent;

    /**
     * 量表总分（服务端汇总）
     */
    private Integer totalScore;
    /**
     * 营养风险：0-无 1-有
     */
    private Integer riskFlag;
    /**
     * 筛查时机（1-入院48小时内 2-病情变化复筛 3-术后复筛 4-定期复筛）
     */
    private Integer screenSource;
    /**
     * 下次筛查日期（阴性 → 筛查日 +7 天；阳性置空）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextScreenDate;

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
     * 筛查人姓名
     */
    private String screenerName;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableLogic
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
