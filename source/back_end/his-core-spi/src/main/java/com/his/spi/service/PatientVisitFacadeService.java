package com.his.spi.service;

/**
 * 就诊次（biz_visit）跨域窄端口：住院收治侧保证患者当日存在一条进行中的就诊次。
 * <p>
 * 数据宿主模块=patient；实现方仅依赖本域 mapper，不牵挂号/住院编排服务，避免 Bean 环。
 */
public interface PatientVisitFacadeService {

    /**
     * 保证患者当日存在一条进行中的就诊次：当天已有 OPEN 就诊次则复用（把挂号ID并入 regist_ids），
     * 没有则新建一条（就诊次取号、起始时间、状态由提供方落库），返回就诊次ID。
     *
     * @param patientId 患者ID
     * @param registId  本次关联的挂号ID（可空）
     * @return 就诊次ID
     */
    Long ensureOpenVisit(Long patientId, Long registId);
}
