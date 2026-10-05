package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 调价历史出参
 */
@Data
public class PriceChangeHistoryVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 项目类型：DRUG-药品 CONSUMABLE-耗材 INSPECTION-检查 LABORATORY-检验 TREATMENT-治疗
     */
    private String itemType;

    /**
     * 项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 项目编码
     */
    private String itemCode;

    /**
     * 项目名称（冗余）
     */
    private String itemName;

    /**
     * 原价
     */
    private BigDecimal oldPrice;

    /**
     * 新价
     */
    private BigDecimal newPrice;

    /**
     * 调价原因
     */
    private String changeReason;

    /**
     * 操作人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 调价时间
     */
    private LocalDateTime changeTime;
}
