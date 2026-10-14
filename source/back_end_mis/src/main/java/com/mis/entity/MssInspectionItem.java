package com.mis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 飞检发现问题明细
 */
@Data
@TableName("mss_inspection_item")
public class MssInspectionItem implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 任务id */
    private Long inspectionId;
    /** 问题类型 */
    private String problemType;
    /** 问题描述 */
    private String problemDesc;
    /** 证据说明 */
    private String evidence;
    /** 涉及金额 */
    private BigDecimal involvedAmount;
    /** 处置方式（整改/追回/移交稽核） */
    private String dispose;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
