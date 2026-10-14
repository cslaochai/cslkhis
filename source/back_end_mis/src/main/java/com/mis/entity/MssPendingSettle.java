package com.mis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待结算业务（门诊就诊 / 出院登记）
 */
@Data
@TableName("mss_pending_settle")
public class MssPendingSettle implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 业务类型（1-门诊 2-住院） */
    private Integer bizType;
    /** 业务号（门诊就诊号/住院号） */
    private String bizNo;
    private String patientName;
    private String idCard;
    private String idCardMask;
    private String insuranceType;
    private String dept;
    private String doctor;
    private Integer admitDays;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bizTime;

    /** 状态（1-待结算 2-已结算） */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
