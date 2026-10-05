package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 处方模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_rx_template")
public class BizRxTemplate extends BaseEntity {

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 药品数量
     */
    private Integer drugCount;
    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    @TableField(exist = false)
    private List<BizRxTemplateDetail> details;
}
