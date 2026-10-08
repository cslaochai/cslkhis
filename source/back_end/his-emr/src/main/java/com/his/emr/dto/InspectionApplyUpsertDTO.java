package com.his.emr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 检查申请开单入参（医生站开单那一刻就落库）。
 */
@Data
@Schema(description = "检查申请开单入参")
public class InspectionApplyUpsertDTO {

    @Schema(description = "申请单ID（有值=修改，为空=新增）")
    private Long id;

    /**
     * 挂号ID
     */
    @NotNull(message = "挂号ID不能为空")
    @Schema(description = "挂号ID")
    private Long registId;

    /**
     * 病历ID
     */
    @Schema(description = "病历ID（病历尚未保存时为空，结诊时回填）")
    private Long recordId;

    /**
     * 患者ID
     */
    @NotNull(message = "患者ID不能为空")
    @Schema(description = "患者ID")
    private Long patientId;

    /**
     * 检查项目ID
     */
    @NotNull(message = "检查项目不能为空")
    @Schema(description = "检查项目ID")
    private Long inspectionItemId;

    /**
     * 检查部位
     */
    @Schema(description = "检查部位")
    private String bodyPart;

    /**
     * 检查目的
     */
    @Schema(description = "检查目的")
    private String inspectionPurpose;

    /**
     * 临床诊断
     */
    @Schema(description = "临床诊断")
    private String clinicalDiagnosis;

    /**
     * 特殊要求
     */
    @Schema(description = "特殊要求")
    private String specialRequirements;

    /**
     * 是否急诊（0-否 1-是）
     */
    @Schema(description = "是否加急（0-否 1-是）")
    private Integer isEmergency;
}
