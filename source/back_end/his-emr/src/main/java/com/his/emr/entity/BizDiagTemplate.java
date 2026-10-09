package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 常用诊断模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_diag_template")
public class BizDiagTemplate extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;
    /**
     * ICD-10编码
     */
    private String icdCode;
    /**
     * 诊断名称
     */
    private String icdName;
    /**
     * 排序
     */
    private Integer sortOrder;
}
