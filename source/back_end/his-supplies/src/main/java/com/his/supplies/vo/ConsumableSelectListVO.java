package com.his.supplies.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 耗材下拉选择VO
 */
@Data
public class ConsumableSelectListVO {
    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 耗材编码（唯一） */
    private String consumableCode;
    /** 耗材名称 */
    private String consumableName;
    /** 规格 */
    private String specification;
    /** 单位（包、支、盒、个等） */
    private String unit;
    /** 零售价 */
    private BigDecimal retailPrice;
}
