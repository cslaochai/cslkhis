package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日间手术随访台账（出院/转住院后 24h 内必访，追加式可多次）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_day_surgery_follow")
public class BizDaySurgeryFollow extends BaseEntity {

    /**
     * 登记单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 随访方式（1-电话 2-门诊 3-上门 4-线上）
     */
    private Integer followType;

    /**
     * 随访结果（1-无异常 2-有异常已处置 3-有异常再就诊 4-失联）
     */
    private Integer result;

    /**
     * 随访内容
     */
    private String content;

    /**
     * 随访人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 随访人姓名
     */
    private String operator;

    /**
     * 随访时间
     */
    private LocalDateTime followTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
