package com.his.clinical.event;

import com.his.clinical.support.QcOperator;

/**
 * 门诊结诊事务提交成功后发布的事件。
 *
 * <p>负载是病历 ID + 结诊医生身份快照：异步收尾（规则质控 + 引导单/处方签 PDF）按 ID 重新查库即可，
 * 不传内存对象是因为异步线程拿不到结诊事务里的状态。
 *
 * <p>为什么要单独带身份快照：质控人取的是登录态，而收尾跑在事务提交之后的独立线程里，
 * 那里没有登录态 —— 不在这里取一次，下游只能拿到 null，质控单落不了库。
 */
public class ClinicSubmittedEvent {

    private final Long recordId;

    /**
     * 结诊医生的身份快照
     */
    private final QcOperator operator;

    public ClinicSubmittedEvent(Long recordId, QcOperator operator) {
        this.recordId = recordId;
        this.operator = operator;
    }

    public Long getRecordId() {
        return recordId;
    }

    public QcOperator getOperator() {
        return operator;
    }
}
