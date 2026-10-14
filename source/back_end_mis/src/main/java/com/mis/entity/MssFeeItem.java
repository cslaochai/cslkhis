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
 * 费用明细（待结算 / 结算单共用：ownerType 1-待结算 2-结算单）
 */
@Data
@TableName("mss_fee_item")
public class MssFeeItem implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 归属类型（1-待结算 2-结算单） */
    private Integer ownerType;
    private Long ownerId;

    private String name;
    private String category;
    private String spec;
    private String unit;
    private String ybCode;
    private String payClass;
    private BigDecimal selfRatio;
    private BigDecimal insuranceRatio;
    private Integer qty;
    private BigDecimal price;
    private BigDecimal amount;
    /** 是否已冲正 */
    private Boolean reversed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
