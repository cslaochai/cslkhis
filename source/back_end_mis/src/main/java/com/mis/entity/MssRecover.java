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
 * 违规追回单
 */
@Data
@TableName("mss_recover")
public class MssRecover implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 追回单号（唯一） */
    private String recoverNo;
    /** 来源类型（1-疑点稽核 2-飞检） */
    private Integer sourceType;
    /** 来源单号 */
    private String sourceNo;
    /** 定点机构编码 */
    private String targetOrgCode;
    /** 定点机构名称 */
    private String targetOrgName;
    /** 所属账期（yyyy-MM） */
    private String month;
    /** 追回金额 */
    private BigDecimal recoverAmount;
    /** 追回原因 */
    private String reason;
    /** 状态（待追回/已追回/已核销） */
    private String status;
    /** 追回时间 */
    private LocalDateTime recoverTime;
    /** 经办人 */
    private String handler;
    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
