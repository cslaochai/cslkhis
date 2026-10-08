package com.his.patient.service;

import java.util.Map;

/**
 * 「今日就诊」提供者 —— 依赖倒置。
 */
public interface PatientTodayVisitProvider {

    /**
     * 当前登录用户视角下、今天有就诊记录的患者。
     *
     * @return patientId → 今日就诊信息；无今日就诊则不返回该 key（不是返回 null 值）
     */
    Map<Long, PatientTodayVisit> todayVisits();
}
