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
 * 医保三大目录
 */
@Data
@TableName("mss_catalog")
public class MssCatalog implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String ybCode;
    private String name;
    private String category;
    private String spec;
    private String unit;
    /** 支付类别（甲/乙/丙） */
    private String payClass;
    /** 乙类先行自付比例（0-1） */
    private BigDecimal selfRatio;
    /** 政策报销比例（0-1） */
    private BigDecimal insuranceRatio;
    private BigDecimal limitPrice;
    /** 状态（1-启用 0-停用） */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
