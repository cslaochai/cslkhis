package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 发药明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dispensing_detail")
public class BizDispensingDetail extends BaseEntity {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispensingId;
    private String dispensingNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long prescriptionDetailId;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;
    /** 药品编码 */
    private String drugCode;
    /** 药品名称 */
    private String drugName;
    /** 规格 */
    private String specification;
    /** 单位 */
    private String unit;
    /** 批号 */
    private String batchNo;
    private BigDecimal quantity;
    /** 单价 */
    private BigDecimal price;
    private BigDecimal amount;
    /** 明细状态（1-正常 2-已入库 3-已取消） */
    private Integer detailStatus;
}
