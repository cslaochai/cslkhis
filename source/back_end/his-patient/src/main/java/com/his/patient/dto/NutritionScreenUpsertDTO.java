package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.InEnum;
import com.his.patient.enums.NutritionScreenTypeEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营养风险筛查/评定登记。
 */
@Data
public class NutritionScreenUpsertDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 入院ID
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 量表（1-NRS2002 2-PG-SGA 3-MNA）
     */
    @NotNull(message = "筛查量表不能为空")
    @InEnum(value = NutritionScreenTypeEnum.class, message = "筛查量表取值不合法（1-NRS2002 2-PG-SGA 3-MNA）")
    private Integer screenType;

    /**
     * NRS2002 营养状态受损评分 0~3（1-体重下降 2-GI手术 3-骨髓移植等）
     */
    @Min(value = 0, message = "受损评分取值 0~3")
    @Max(value = 3, message = "受损评分取值 0~3")
    private Integer impairScore;

    /**
     * NRS2002 疾病严重程度评分 0~3（1-髋骨骨折 2-腹部大手术 3-颅脑损伤）
     */
    @Min(value = 0, message = "严重度评分取值 0~3")
    @Max(value = 3, message = "严重度评分取值 0~3")
    private Integer severityScore;

    /**
     * 年龄评分（≥70 岁 1 分，其余 0 分；服务端按患者年龄兜底校验）
     */
    @Min(value = 0, message = "年龄评分取值 0~1")
    @Max(value = 1, message = "年龄评分取值 0~1")
    private Integer ageScore;

    /**
     * PG-SGA / MNA 由评定人给出的总分（NRS2002 忽略此字段，一律按分项汇总）
     */
    @Min(value = 0, message = "总分不能为负")
    private Integer totalScore;

    /**
     * 身高 cm
     */
    private BigDecimal heightCm;

    /**
     * 体重 kg
     */
    private BigDecimal weightKg;

    /**
     * 近 3 个月体重下降百分比（%）
     */
    private BigDecimal weightLossPercent;

    /**
     * 筛查时机（1-入院48小时内 2-病情变化复筛 3-术后复筛 4-定期复筛）
     */
    @NotNull(message = "筛查时机不能为空")
    @Min(value = 1, message = "筛查时机取值不合法")
    @Max(value = 4, message = "筛查时机取值不合法")
    private Integer screenSource;

    /**
     * 筛查时间，为空取当前时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime screenTime;

    /**
     * 下次筛查日期；NRS2002 判阴性时服务端按 +7 天兜底，阳性置空
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextScreenDate;

    /**
     * 分项明细 JSON（评定的条目留痕）
     */
    private String itemsJson;

    /**
     * 备注
     */
    private String remark;
}
