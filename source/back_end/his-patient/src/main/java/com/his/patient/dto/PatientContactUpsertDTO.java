package com.his.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者联系人新增/修改入参
 */
@Data
@Schema(description = "患者联系人新增/修改入参")
public class PatientContactUpsertDTO {

    /**
     * 主键ID
     */
    @Schema(description = "联系人ID，新增时为空，修改时必填")
    private Long id;

    /**
     * 患者ID
     */
    @Schema(description = "患者ID，修改时可空（以库中记录为准）")
    private Long patientId;

    /**
     * 联系人姓名
     */
    @NotBlank(message = "联系人姓名不能为空")
    @Schema(description = "联系人姓名")
    private String contactName;

    /**
     * 与患者关系。
     *
     * <p>⚠ 这里是**码值**（字典患者关系字典：1-本人 2-配偶 3-父亲 … 99-其他），
     * 不是「配偶」这样的文本。表列是 {@code tinyint}，此前 DTO 声明成 String 并注上
     * 「如：父亲、母亲、配偶、子女」，于是「配偶」被 MySQL 隐式转成 0 落库 —— 页面上
     * 关系显示成 0，且不报任何错。要文案请读出参的 {@code relationshipText}。
     */
    @NotNull(message = "与患者关系不能为空")
    @Schema(description = "与患者关系码值（字典 sys_patient_relation：2-配偶 3-父亲 … 99-其他）")
    private Integer relationship;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 是否主要联系人（0-否 1-是）
     */
    @Schema(description = "是否主要联系人：0-否 1-是")
    private Integer isPrimary;

    /**
     * 联系地址
     */
    @Schema(description = "联系地址")
    private String address;

    /**
     * 状态（0-停用 1-启用）
     */
    @Schema(description = "状态：0-停用 1-启用")
    private Integer status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
