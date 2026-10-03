package com.his.fee.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 某次就诊按项目类型聚合的应收净额（L1 读出口）。
 *
 * <p>引导单、科室报表要的是"这次就诊一共发生了多少钱"，因此按类型分组返回而不是只给一个总额：
 * 一个总额没法解释"钱花在哪"，而把每项现拼再 SUM 又会在红冲负行上进出几分钱。
 */
@Data
public class FeeTypeSumVO {

    /** 项目类型（字典 {@code his_charge_item_type}） */
    private Integer itemType;

    /** 该类型净额（含红冲负行，已全额红冲的行不计） */
    private BigDecimal amount;
}
