package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 药房盘点明细出参
 */
@Data
public class StocktakeItemVO {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 盘点单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stocktakeId;

    /** 库存批次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;

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

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate productionDate;

    /** 有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    /** 库位 */
    private String location;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 快照时已锁定数量 */
    private BigDecimal lockedQuantity;

    /** 账面数量（快照时点） */
    private BigDecimal bookQuantity;

    /** 实盘数量（null=尚未录入） */
    private BigDecimal countedQuantity;

    /** 差异数量 */
    private BigDecimal diffQuantity;

    /** 差异金额（元） */
    private BigDecimal diffAmount;

    /** 当前批次余额（复核时对账用：快照之后可能又发过药） */
    private BigDecimal currentQuantity;

    /** 过账标记（0-未过账 1-已盘盈亏过账 2-无差异免过账） */
    private Integer posted;

    /** 差异方向文案（未录入/无差异/盘盈/盘亏） */
    private String diffTypeText;

    /** 差异说明 */
    private String remark;
}
