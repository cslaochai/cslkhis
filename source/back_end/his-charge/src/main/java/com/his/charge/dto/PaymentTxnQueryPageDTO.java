package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 支付流水分页查询入参（L3 台账）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentTxnQueryPageDTO extends PageParam {

    /**
     * 关键字：流水号 / 账单号 / 患者姓名 / 渠道流水号 / 备注模糊匹配
     * （备注参与检索是为了能按「小程序微信充值（支付单 PAY…）」捞出患者端押金流水）
     */
    private String keyword;

    /**
     * 结算账单ID
     */
    private Long billId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 资金方向（字典 his_pay_direction：1-收款 2-退款）
     */
    private Integer direction;

    /**
     * 支付方式（1-现金 2-微信 3-支付宝 4-医保个人账户 5-院内余额 6-银行卡 7-转账）
     */
    private Integer payMethod;

    /**
     * 流水状态（字典 his_pay_txn_status：1-成功 2-已冲正）
     */
    private Integer txnStatus;

    /**
     * 流水来源（字典 his_txn_source）
     */
    private Integer sourceType;

    /**
     * 收银人员工ID（班结归集主键）
     */
    private Long cashierId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
