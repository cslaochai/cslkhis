package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 药品入库单明细出参（药品名称等为写入时快照，不联表）
 */
@Data
public class DrugInboundDetailVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long inboundId;

    /** 入库单号（唯一） */
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

    private String batchNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate productionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    private BigDecimal quantity;

    private BigDecimal costPrice;

    private BigDecimal amount;

    /** 明细状态（1-正常 2-已入库 3-已取消） */
    private Integer detailStatus;

    /** 备注 */
    private String remark;
}
