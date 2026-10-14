package com.mis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 参保人（医保侧参保库）
 */
@Data
@TableName("mss_insured_person")
public class JinbInsuredPerson implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String idCard;
    private String insuranceNo;
    private String name;
    private String gender;
    /** 险种（2-城镇职工医保 3-城乡居民医保） */
    private String insuranceType;
    /** 参保状态（1-正常参保 0-停保） */
    private Integer personStatus;
    private BigDecimal accountBalance;
    private String region;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
