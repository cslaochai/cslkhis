package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 患者联系人出参
 */
@Data
@Schema(description = "患者联系人")
public class PatientContactVO {

    /**
     * 主键ID
     */
    @Schema(description = "联系人ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 患者ID
     */
    @Schema(description = "患者ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 联系人姓名
     */
    @Schema(description = "联系人姓名")
    private String contactName;

    /**
     * 与患者关系（如：父母、配偶、子女、朋友等）
     */
    @Schema(description = "与患者关系码值（字典 sys_patient_relation）")
    private Integer relationship;

    /**
     * 与患者关系文案（后端按字典翻译）。
     *
     * <p>为什么后端给而不让前端自己查字典：这一组的「关系」在库里有两个字段 ——
     * 结构化表患者联系方式.relationship 是 tinyint 码值，
     * 主档患者基本信息.contact_relation 是 varchar 标签。若让前端各查一次字典，
     * 就会出现两处渲染口径；历史上主档那一列正因为没人翻译，把码值 "2" 当成关系名显示了出来。
     * 命中不了字典的码值返回空串，**不回落**成「其他」这类看似合法的值。
     */
    @Schema(description = "与患者关系文案（字典翻译，未知码值返回空串）")
    private String relationshipText;

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
