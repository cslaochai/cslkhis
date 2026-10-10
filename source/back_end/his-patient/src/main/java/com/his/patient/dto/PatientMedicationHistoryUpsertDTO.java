package com.his.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 患者既往用药史新增/修改入参
 */
@Data
@Schema(description = "患者既往用药史新增/修改入参")
public class PatientMedicationHistoryUpsertDTO {

    /**
     * 主键ID
     */
    @Schema(description = "用药史ID，新增时为空，修改时必填")
    private Long id;

    /**
     * 患者ID
     */
    @Schema(description = "患者ID，修改时可空（以库中记录为准）")
    private Long patientId;

    /**
     * 药物名称
     */
    @NotBlank(message = "药物名称不能为空")
    @Schema(description = "药物名称")
    private String drugName;

    /**
     * 药物类型（处方药/非处方药/中药/保健品）
     */
    @Schema(description = "药物类型（处方药/非处方药/中药/保健品）")
    private String drugType;

    /**
     * 剂量
     */
    @Schema(description = "剂量（如 0.5g）")
    private String dosage;

    /**
     * 频次
     */
    @Schema(description = "频次（如 qd / bid）")
    private String frequency;

    /**
     * 给药途径（口服/注射/外用/吸入等）
     */
    @Schema(description = "给药途径（口服/注射/外用/吸入）")
    private String route;

    /**
     * 开始用药日期
     */
    @NotNull(message = "开始用药日期不能为空")
    @Schema(description = "开始用药日期")
    private LocalDate startDate;

    /**
     * 停药日期
     */
    @Schema(description = "停药日期")
    private LocalDate endDate;

    /**
     * 用药指征/适应症
     */
    @Schema(description = "用药指征 / 适应症")
    private String indications;

    /**
     * 处方医生
     */
    @Schema(description = "处方医生")
    private String prescriber;

    /**
     * 用药状态（进行中/已停用/已换药/已减量）
     */
    @Schema(description = "用药状态（进行中/已停用/已换药/已减量）")
    private String status;

    /**
     * 停药原因（疗效不佳/不良反应/患者要求/已治愈）
     */
    @Schema(description = "停药原因（疗效不佳/不良反应/患者要求/已治愈）")
    private String reasonStop;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
