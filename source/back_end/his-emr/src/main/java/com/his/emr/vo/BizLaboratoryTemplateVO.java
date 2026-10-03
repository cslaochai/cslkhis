package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 检验申请模板出参
 */
@Data
public class BizLaboratoryTemplateVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 所属医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 检验项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryItemId;

    /**
     * 检验项目编码
     */
    private String laboratoryItemCode;

    /**
     * 检验项目名称
     */
    private String laboratoryItemName;

    /**
     * 标本类型（如静脉血、尿液）
     */
    private String sampleType;

    /**
     * 检验目的
     */
    private String inspectionPurpose;

    /**
     * 是否急诊：0-否 1-是
     */
    private Integer isEmergency;

    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;
}
