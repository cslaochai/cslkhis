package com.his.charge.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资金账户流水分页查询入参（L3 台账）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FundTxnQueryPageDTO extends PageParam {

    /**
     * 账户ID（看某账户的进出明细）
     */
    private Long accountId;

    /**
     * 患者ID（跨账户看这个人全部账户的流水）
     */
    private Long patientId;

    /**
     * 流水类型（字典 his_account_txn_type：1-预交金充值 2-预交金退款 3-余额支付扣减 4-余额退款入账 5-出院结算退差入账 6-手工调整）
     */
    private Integer txnType;

    /**
     * 流水状态（1-有效 2-已冲正，与支付流水同语义）
     */
    private Integer txnStatus;
}
