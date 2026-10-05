package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 患者既往用药史出参
 */
@Data
@Schema(description = "患者既往用药史")
public class PatientMedicationHistoryVO {

    /**
     * 主键ID
     */
    @Schema(description = "用药史ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 患者ID
     */
    @Schema(description = "患者ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 药物名称
     */
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
    @Schema(description = "剂量")
    private String dosage;

    /**
     * 频次
     */
    @Schema(description = "频次")
    private String frequency;

    /**
     * 给药途径（口服/注射/外用/吸入等）
     */
    @Schema(description = "给药途径")
    private String route;

    /**
     * 开始用药日期
     */
    @Schema(description = "开始用药日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 停药日期
     */
    @Schema(description = "停药日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
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
    @Schema(description = "停药原因")
    private String reasonStop;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
