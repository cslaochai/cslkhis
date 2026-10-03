package com.his.patient.service;

import java.util.Map;

/**
 * 「今日就诊」提供者 —— 依赖倒置。
 *
 * <p>接口定义在**调用方**（患者域，全局患者搜索需要按今日就诊排序），
 * 实现放在**被依赖方**（就诊域 his-appoint，它能访问候诊队列）。
 * his-appoint 已依赖 his-patient，因此不存在反向依赖，也不会有循环依赖。
 *
 * <p>消费方必须用 {@code ObjectProvider.getIfAvailable()} 拿实现并允许为空：
 * 就诊域缺席时只是「不置顶」，患者搜索本身必须照常可用（降级不阻断）。
 */
public interface PatientTodayVisitProvider {

    /**
     * 当前登录用户视角下、今天有就诊记录的患者。
     *
     * @return patientId → 今日就诊信息；无今日就诊则不返回该 key（不是返回 null 值）
     */
    Map<Long, PatientTodayVisit> todayVisits();
}
