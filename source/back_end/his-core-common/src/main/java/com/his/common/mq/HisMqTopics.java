package com.his.common.mq;

/**
 * RocketMQ Topic 常量。
 * <p>
 * 命名规则：{@code HIS_业务域_事件}，全大写下划线。
 * Tag 在生产者发送时按动作细分（如 {@code bill_paid} / {@code bill_refunded}），
 * 消费者按 Tag 订阅，避免一个 Topic 塞太多不相关事件。
 */
public final class HisMqTopics {

    /**
     * 操作审计日志异步落库。消费者批量刷 sys_audit_log。
     */
    public static final String AUDIT_LOG = "HIS_AUDIT_LOG";
    /**
     * 收费账单已支付成功。事务提交后发出，fan-out 给通知、指标、药房等下游。
     */
    public static final String CHARGE_BILL_PAID = "HIS_CHARGE_BILL_PAID";
    /**
     * 审计日志消费者组。
     */
    public static final String CG_AUDIT_LOG = "HIS_CG_AUDIT_LOG";

    // ===== 消费者组（consumer group）=====
    /**
     * 收费账单支付事件消费者组。
     */
    public static final String CG_CHARGE_BILL_PAID = "HIS_CG_CHARGE_BILL_PAID";
    public static final String PG_DEFAULT = "HIS_PG_DEFAULT";

    // ===== 生产组（producer group）=====

    private HisMqTopics() {
    }
}
