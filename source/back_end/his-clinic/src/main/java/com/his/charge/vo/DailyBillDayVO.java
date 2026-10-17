package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 日清单里的一天。
 */
@Data
public class DailyBillDayVO implements Serializable {

    /**
     * 日期（yyyy-MM-dd）
     */
    private String date;

    /**
     * 当日小计
     */
    private BigDecimal dayTotal;

    /**
     * 当日明细
     */
    private List<DailyBillItemVO> items;
}
