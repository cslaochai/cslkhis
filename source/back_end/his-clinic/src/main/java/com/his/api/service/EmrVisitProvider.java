package com.his.api.service;

import com.his.api.vo.AppointBriefVO;
import com.his.api.vo.PatientBriefVO;

/**
 * 病历-就诊联动端口
 */
public interface EmrVisitProvider {

    /**
     * 按挂号ID取挂号摘要。
     *
     * @param registId 挂号ID
     * @return 挂号摘要；不存在返回 {@code null}
     */
    AppointBriefVO getAppointById(Long registId);

    /**
     * 按患者ID取患者摘要。
     *
     * @param patientId 患者ID
     * @return 患者摘要；不存在返回 {@code null}
     */
    PatientBriefVO getPatientById(Long patientId);

    /**
     * 结诊联动：挂号置「已完成」、队列置「已完成」并回写结束时间。
     * 只对本次就诊做状态收口，不涉及收费（收费联动走 ChargeApi）。
     *
     * @param registId 挂号ID
     * @param queueId  队列ID（可为 null，无队列时只收口挂号）
     * @return 该队列的接诊医生ID（无队列或队列不存在时返回 {@code null}），供调用方清医生接诊状态缓存
     */
    Long completeVisit(Long registId, Long queueId);
}
