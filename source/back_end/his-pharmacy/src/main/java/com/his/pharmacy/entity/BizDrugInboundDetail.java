package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 药品入库单明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_drug_inbound_detail")
public class BizDrugInboundDetail extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /** 入库单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inboundId;

    /** 入库单号（冗余，便于明细单独查询） */
    private String inboundNo;

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
    private LocalDate productionDate;

    /** 有效期 */
    private LocalDate expiryDate;

    /** 入库数量 */
    private BigDecimal quantity;

    /** 成本价（进价） */
    private BigDecimal costPrice;

    /** 金额 = 数量 × 成本价 */
    private BigDecimal amount;

    /** 明细状态（1-正常 2-已入库 3-已取消） */
    private Integer detailStatus;
}
