package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 日清单里的一天。
 *
 * <p>{@code dayTotal} 由服务端按"当天明细金额之和"算出，前端不许再累加一遍 ——
 * 前端累加会漏掉退费行，日清单就成了第二套算法。
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
