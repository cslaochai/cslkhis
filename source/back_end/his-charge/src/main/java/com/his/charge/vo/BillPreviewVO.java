package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.charge.entity.BizSettlementBillItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算试算出参（L2）：与结算同一套计算（共用草稿），只是不落库。
 *
 * <p><b>本 VO 里的数不是"预估"，而是"现在出账会写成多少"</b>：任何一处与结算不同的算法
 * 都会在两个数之间留出漂移空间，而小票与结算单对不上正是这么来的。
 * 所以调用方不许照本 VO 自己再算一遍统筹/自付，只按这些字段展示。
 *
 * <p>{@code payableAmount} 是<b>患者该掏的钱</b>（已减优惠、统筹、个账）；
 * 统筹 {@code poolAmount} 是医保局后付的钱，绝不进本院现金抽屉，也绝不并进实收。
 */
@Data
public class BillPreviewVO {

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 就诊类型（字典 his_encounter_type：1-门诊 2-住院）
     */
    private Integer encounterType;

    /**
     * 就诊标识
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long encounterId;

    /**
     * 纳入本次结算的记账行数
     */
    private Integer feeCount;

    /**
     * 应收合计（医保前总价）
     */
    private BigDecimal totalAmount;

    /**
     * 院内优惠/抹零
     */
    private BigDecimal discountAmount;

    /**
     * 医保统筹（后付给医保局，不是支付方式）
     */
    private BigDecimal poolAmount;

    /**
     * 医保个人账户支付（刷参保人卡，是一笔真实收款）
     */
    private BigDecimal accountAmount;

    /**
     * 自付合计（= 应收 − 统筹，还<b>没</b>减优惠与个账）：它按记账行原额统计，
     * 与下面 {@code payableAmount} 相差"优惠 + 个账"两段。页面上别把这两个数并排当同一个意思，
     * 有优惠的账单两者必然不等。
     */
    private BigDecimal selfAmount;

    /**
     * 患者应缴 = total - discount - pool - account
     */
    private BigDecimal payableAmount;

    /**
     * 结算方式（字典 his_settlement_mode：1-自费 2-医保）
     */
    private Integer settlementMode;

    private String settlementModeText;

    /**
     * 险种（医保时带出，自费为空）
     */
    private String insuranceType;

    /**
     * 报销比例（百分比数值，85 表示 85%）—— 页面上要写清"按什么比例报"，
     * 否则试算的统筹额看起来像凭空冒出来的数
     */
    private BigDecimal coverageRatio;

    /**
     * 逐行 split 快照（与真正出账时写入结算账单行的内容一致）
     */
    private List<BizSettlementBillItem> items;
}
