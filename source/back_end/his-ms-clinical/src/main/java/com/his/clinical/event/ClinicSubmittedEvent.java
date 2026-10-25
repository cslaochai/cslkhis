package com.his.clinical.event;

/**
 * 门诊结诊事务提交成功后发布的事件。
 *
 * <p>负载只有病历 ID：异步收尾（规则质控 + 引导单/处方签 PDF）按 ID 重新查库即可，
 * 不传内存对象是因为异步线程拿不到结诊事务里的 EntityManager 状态。
 */
public class ClinicSubmittedEvent {

    private final Long recordId;

    public ClinicSubmittedEvent(Long recordId) {
        this.recordId = recordId;
    }

    public Long getRecordId() {
        return recordId;
    }
}
