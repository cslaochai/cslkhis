package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发药明细出参
 */
@Data
public class BizDispensingDetailVO {
    /** 发药明细ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 发药单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispensingId;
    /** 发药单号 */
    private String dispensingNo;
    /** 处方明细ID */
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
    /** 发药数量，单位：最小包装单位 */
    private BigDecimal quantity;
    /** 单价，单位：元 */
    private BigDecimal price;
    /** 金额，单位：元 */
    private BigDecimal amount;
    /** 明细状态（1-正常 2-已入库 3-已取消） */
    private Integer detailStatus;
}
