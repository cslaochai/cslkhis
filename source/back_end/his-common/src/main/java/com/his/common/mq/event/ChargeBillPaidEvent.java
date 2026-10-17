package com.his.common.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账单支付成功事件。
 * <p>
 * 在 {@code PaymentService.pay()} 事务提交后发出，
 * 下游（通知、指标刷新、药房待摆药等）各自订阅，互不阻塞。
 * <p>
 * 注意：这里只放<strong>只读</strong>的业务快照，不包含任何会被后续修改的状态字段；
 * 消费方需要最新数据时应按 billId 回查，不要信任事件里的数字。
 * <p>
 * {@code createById/createBy} 由 HisMqPublisher 在发送时自动从当前线程取——
 * 记录"是哪个收费员/自助患者完成了这次支付"，下游通知/指标按此归因。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeBillPaidEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 结算账单主键。
     */
    private Long billId;

    /**
     * 账单号（便于日志/排查）。
     */
    private String billNo;

    /**
     * 患者主键。
     */
    private Long patientId;

    /**
     * 就诊类型：1-门诊 2-住院。
     */
    private Integer encounterType;

    /**
     * 本次实付合计（患者自付部分）。
     */
    private BigDecimal paidAmount;

    /**
     * 账单总金额。
     */
    private BigDecimal totalAmount;

    /**
     * 支付时间。
     */
    private LocalDateTime paidTime;

    /**
     * 本次收款的流水明细（支付方式 + 金额）。
     */
    private List<PaidTxn> txns;

    /**
     * 触发本次支付的操作人 ID（收费员 employeeId / 患者 userId）。
     */
    private Long createById;

    /**
     * 触发本次支付的操作人姓名。
     */
    private String createBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaidTxn implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 支付方式码（PaymentMethodEnum）。
         */
        private Integer payMethod;
        private BigDecimal amount;
        /**
         * 渠道流水号（现金/院内余额可能为空）。
         */
        private String channelTxnNo;
    }
}
