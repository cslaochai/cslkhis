package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 门诊病历修改日志（门诊病历修改日志）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_medical_record_log")
public class BizMedicalRecordLog extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;
    /**
     * 病历号
     */
    private String recordNo;

    /**
     * 操作人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    /**
     * 操作人姓名
     */
    private String userName;
    /**
     * 操作类型
     */
    private String operation;
    /**
     * 修改字段
     */
    private String fieldName;
    /**
     * 修改前值
     */
    private String oldValue;
    /**
     * 修改后值
     */
    private String newValue;
}
