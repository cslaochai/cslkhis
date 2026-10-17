package com.his.charge.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算入参：把一批待结算记账行锁成一张账单。
 */
@Data
public class BillSettleUpsertDTO {

    /**
     * 就诊类型（字典 his_encounter_type：1-门诊 2-住院）
     */
    @NotNull(message = "缺少就诊类型")
    @AllowedValues(values = {"1", "2"}, message = "就诊类型不合法")
    private Integer encounterType;

    /**
     * 就诊标识：门诊=挂号ID，住院=入院ID
     */
    @NotNull(message = "缺少就诊标识")
    private Long encounterId;

    /**
     * 本次纳入结算的记账行ID；为空 = 该就诊下全部「1-待结算」行（收费台"全部结算"）
     */
    private List<Long> feeIds;

    /**
     * 账单类型（字典 his_bill_type：1-挂号费结算 2-门诊诊间结算 3-住院中途结算 4-出院结算）；
     * 为空按就诊类型推（门诊→2，住院→3）
     */
    @NotNull(message = "请选择账单类型")
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "账单类型不合法")
    private Integer billType;

    /**
     * 结算方式（字典 his_settlement_mode：1-自费 2-医保）；为空按患者有无医保号推
     */
    @NotNull(message = "请选择结算方式")
    @AllowedValues(values = {"1", "2"}, message = "结算方式取值不合法")
    private Integer settlementMode;

    /**
     * 院内优惠/抹零（只放真优惠；医保统筹走 split，绝不写这里）
     */
    @DecimalMin(value = "0", message = "优惠金额不能为负")
    private BigDecimal discountAmount;

    /**
     * 备注
     */
    private String remark;
}
