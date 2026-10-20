package com.his.spi.service;

import com.his.spi.dto.TechAuthGateDTO;

/**
 * 医疗技术准入闸端口：开单/申请前问一句「这个人此刻有没有做这台（项）的授权」。
 *
 * <p>授权台账、审批、收回的编排仍归医技域自己；本端口只承诺准入判定，返回值不承载展示字段。
 */
public interface EmployeeTechAuthFacadeService {

    /**
     * 技术授权准入闸。
     *
     * <p>用于日间手术申请、手术申请、住院医嘱开立等「落到具体术式/项目」的动作前置校验：
     * 有授权放行；无授权且非急诊直接抛业务异常拦下；无授权但属急诊则登记一笔越权后放行。
     *
     * @param gate 准入判定入参：人、术式/项目类别、所需级别、操作日期与越权登记用的来源单据
     * @throws com.his.common.exception.BusinessException 无授权且非急诊时抛出，文案带项目名与拦截原因
     */
    void gate(TechAuthGateDTO gate);
}
