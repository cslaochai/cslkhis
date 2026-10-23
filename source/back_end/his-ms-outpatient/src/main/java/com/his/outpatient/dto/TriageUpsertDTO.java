package com.his.outpatient.dto;

import com.his.common.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 保存门诊分诊（分诊台右侧分诊卡提交）。
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
     * 分诊等级（1-危重 2-急症 3-亚急 4-非急）。
     * <p>门诊版可空：门诊分诊台已不做四级分级，前端不再提交；传了仍会校验取值，
     * 用于保留急诊等特殊场景的兼容调用。
     */
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "分诊等级取值不合法")
    @Schema(description = "分诊等级（1-危重 2-急症 3-亚急 4-非急，门诊版可空）")
    private Integer triageLevel;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
