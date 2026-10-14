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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 经办结算单（窗口即时结算产生）
 */
@Data
@TableName("mss_bill")
public class MssBill implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String billNo;
    private Long pendingId;
    private String bizNo;
    private Integer admitDays;
    private String patientName;
    private String idCard;
    private String idCardMask;
    private String insuranceType;
    /** 结算类型（门诊/住院） */
    private String settleType;
    private String dept;
    private String doctor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime settleTime;

    /** 状态（已结算/待审核/已退费/异常） */
    private String status;

    private BigDecimal totalAmount;
    private BigDecimal insurancePay;
    private BigDecimal personalAccount;
    private BigDecimal cashPay;
    private BigDecimal selfPay;
    private BigDecimal deductible;
    private BigDecimal basePool;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
