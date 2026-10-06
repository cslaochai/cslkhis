package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 飞检批次名下扣款单聚合（内部拼装用：批次列表要显示「几张单、多少钱」）。
 */
@Data
public class YbInspectionDeductCountVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionId;

    private Integer deductCount;

    private BigDecimal deductAmountSum;
}
