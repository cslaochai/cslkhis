package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预交金余额出参。
 *
 * <p>三个数各是一条链，别把它们当成一个等式：{@code rechargeTotal}/{@code refundTotal} 取
 * L3 里没有账单锚的那段预交现金流（{@code bill_id IS NULL + source_type=3}），
 * {@code balance} 取的是住院资金账户净额 —— 出院结算的余额抵扣与退差会从账户里把钱搬走，
 * 但它们是账户之间的划转，不是"又退了一笔预交金"。所以结算之后
 * {@code balance < rechargeTotal - refundTotal} 是**正常的**，硬凑成减法反而把退差算成现金流出，
 * 收银台当天会凭空少一笔抽屉里的钱。
 */
@Data
public class PrepayBalanceVO implements Serializable {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 充值合计
     */
    private BigDecimal rechargeTotal;

    /**
     * 柜面退款合计（正数；出院退差转入院内余额的不算在这里）
     */
    private BigDecimal refundTotal;

    /**
     * 住院资金账户余额（充值 − 退款 − 结算抵扣 − 出院退差，由账户流水 SUM 现算）
     */
    private BigDecimal balance;

    /**
     * 流水条数
     */
    private Integer flowCount;
}
