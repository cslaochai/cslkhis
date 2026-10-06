package com.his.charge.vo;

import com.his.charge.service.SettlementBillService;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 住院出院结算试算（L2 账单口径 + L3 余额抵扣）。
 *
 * <p><b>钱一律来自 {@code SettlementBillService.previewSettlement}</b>，本类只是把
 * "这次住院还剩多少没结" 与 "住院账户里有多少钱" 两个事实摆在一起，
 * 再算出抵扣/退差/欠费三个数：结算出账走同一份草稿，所以这里说的数与账单开出的数必然一致。
 * 旧实现逐张收费单调 {@code calcSettlementPreview} 再汇总、出账时 L2 又算一遍，
 * 两套算法一漂移，患者手里的小票就和结算单对不上。
 */
@Data
public class InpatientSettlementPreviewVO implements Serializable {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 本次纳入结算的记账行数（不是"费用单数"：四层后应收的事实是记账行）
     */
    private Integer feeCount;

    /**
     * 待结算的记账行（红冲负行也在内，与 L2 草稿同一批）
     */
    private List<DailyBillItemVO> feeRows;

    /**
     * 应收合计（记账行净额）
     */
    private BigDecimal totalAmount;

    /**
     * 院内优惠/抹零
     */
    private BigDecimal discountAmount;

    /**
     * 医保统筹（后付给医保局的钱，不是收银员收的）
     */
    private BigDecimal poolAmount;

    /**
     * 医保个人账户（刷参保人卡扣的额度，是一笔真实收款）
     */
    private BigDecimal accountAmount;

    /**
     * 患者自付（应收 − 统筹，未减优惠与个账）
     */
    private BigDecimal selfAmount;

    /**
     * 应缴合计（应收 − 优惠 − 统筹 − 个账），L3 收的就是这一段
     */
    private BigDecimal payableAmount;

    /**
     * 住院账户当前余额（可用预交金）
     */
    private BigDecimal prepayBalance;

    /**
     * 本次将从余额抵扣（min(应缴, 余额)）
     */
    private BigDecimal balanceUsed;

    /**
     * 结算后应退（余额抵完还剩的钱，转入患者院内余额）
     */
    private BigDecimal refundAmount;

    /**
     * 结算后欠费（应缴抵不完的部分）
     */
    private BigDecimal arrearsAmount;

    /**
     * 结算方式：1-自费 2-医保
     */
    private Integer settleMode;

    /**
     * 结算方式文案（后端给，前端不判码值）
     */
    private String settleModeText;

    /**
     * 医保类型
     */
    private String insuranceType;

    /**
     * 是否会产生欠费（住院账户不足）
     */
    private Boolean willArrears;

    /**
     * 一句话结论（前端直接展示，避免前端自己拼文案拼出第三套口径）
     */
    private String conclusionText;
}
