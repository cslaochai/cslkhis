package com.his.operation.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 治疗项目字典取价行（BizOperationChargeItemMapper#selectTreatmentItem 的返回）。
 */
@Data
public class TreatmentItemPriceVO implements Serializable {

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 单价
     */
    private BigDecimal price;
}
