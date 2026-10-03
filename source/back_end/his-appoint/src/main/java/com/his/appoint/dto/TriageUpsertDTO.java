package com.his.appoint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 保存门诊分诊（分诊台右侧分诊卡提交）。
 *
 * <p>体征项全部允许为空 —— 分诊现场不是所有项目都能测到（比如患者无法测血压）。
 * 唯一必填的是 {@code triageLevel}：分诊的核心产出就是分级，没有等级的分诊没有意义。
 */
@Data
@Schema(description = "门诊分诊保存入参")
public class TriageUpsertDTO {

    /**
     * 队列ID
     */
    @NotNull(message = "队列ID不能为空")
    @Schema(description = "队列ID")
    private Long queueId;

    /**
     * 体温(℃)
     */
    @DecimalMin(value = "30.0", message = "体温不在合理区间")
    @DecimalMax(value = "45.0", message = "体温不在合理区间")
    @Schema(description = "体温(℃)")
    private BigDecimal temperature;

    /**
     * 脉搏(次/分)
     */
    @Min(value = 0, message = "脉搏不在合理区间")
    @Max(value = 300, message = "脉搏不在合理区间")
    @Schema(description = "脉搏(次/分)")
    private Integer pulse;

    /**
     * 呼吸(次/分)
     */
    @Min(value = 0, message = "呼吸不在合理区间")
    @Max(value = 100, message = "呼吸不在合理区间")
    @Schema(description = "呼吸(次/分)")
    private Integer respiration;

    /**
     * 收缩压(mmHg)
     */
    @Min(value = 0, message = "收缩压不在合理区间")
    @Max(value = 400, message = "收缩压不在合理区间")
    @Schema(description = "收缩压(mmHg)")
    private Integer systolicBp;

    /**
     * 舒张压(mmHg)
     */
    @Min(value = 0, message = "舒张压不在合理区间")
    @Max(value = 300, message = "舒张压不在合理区间")
    @Schema(description = "舒张压(mmHg)")
    private Integer diastolicBp;

    /**
     * 血氧饱和度(%)
     */
    @Min(value = 0, message = "血氧饱和度不在合理区间")
    @Max(value = 100, message = "血氧饱和度不在合理区间")
    @Schema(description = "血氧饱和度(%)")
    private Integer spo2;

    /**
     * 身高(cm)
     */
    @DecimalMin(value = "0.0", message = "身高不在合理区间")
    @DecimalMax(value = "300.0", message = "身高不在合理区间")
    @Schema(description = "身高(cm)")
    private BigDecimal height;

    @DecimalMin(value = "0.0", message = "体重不在合理区间")
    @DecimalMax(value = "500.0", message = "体重不在合理区间")
    @Schema(description = "体重(kg)")
    private BigDecimal weight;

    /**
     * 疼痛评分
     */
    @Min(value = 0, message = "疼痛评分不在合理区间")
    @Max(value = 10, message = "疼痛评分不在合理区间")
    @Schema(description = "疼痛评分(0~10)")
    private Integer painScore;

    /**
     * 主诉
     */
    @Schema(description = "主诉")
    private String chiefComplaint;

    /**
     * 分诊等级（1-危重 2-急症 3-亚急 4-非急）
     */
    @NotNull(message = "分诊等级不能为空")
    @Min(value = 1, message = "分诊等级只能是 1~4")
    @Max(value = 4, message = "分诊等级只能是 1~4")
    @Schema(description = "分诊等级（1-危重 2-急症 3-亚急 4-非急）")
    private Integer triageLevel;

    // 原来这里有一个 roomId（"不传则沿用排班诊室"），已删除：诊室**不由分诊台决定**。
    // 唯一来源是排班（医生-诊室绑定在排班上，排队号前缀也取自排班诊室），
    // 分诊卡只读展示。护士要换诊室去改排班 —— 改排班时会把该班次未结束的队列行一起搬。
    // 保留一个「可传但不生效」的字段等于留一条能绕过口径的路，所以直接删掉。

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
