package com.his.emr.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 检查申请开单入参（医生站开单那一刻就落库）。
 *
 * <p>只收「医生填的东西」：项目、部位、目的、临床诊断。患者信息、科室医生快照、
 * 项目编码名称、**价格**一律由后端按 registId + 字典补全 ——
 * 价格让前端传等于把定价权交给浏览器。
 *
 * <p>{@code recordId} 允许为空：患者刚挂号、病历还没保存时开单，结诊时由
 * {@code saveMedicalRecord} 回填。
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
