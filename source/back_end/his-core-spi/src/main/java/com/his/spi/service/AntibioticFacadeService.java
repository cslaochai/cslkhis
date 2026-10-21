package com.his.spi.service;

import java.util.List;

/**
 * 抗菌药处方权端口：医师授权级别必须 ≥ 药品分级，校验不通过直接抛业务异常让整张处方回滚。
 *
 * <p>为什么是抛异常而不是返回码：开方事务里「一张开出去但没权限的处方」比「开不出去」危险得多。
 */
public interface AntibioticFacadeService {

    /**
     * 开方落库前的硬校验：医师是否具备开这些抗菌药的资质。
     *
     * @param doctorId          医师ID
     * @param antibioticDrugIds 本次要开的抗菌药药品ID集合；为空直接放过
     * @throws com.his.common.exception.BusinessException 无有效授权时抛出，整单回滚
     */
    void assertCanPrescribe(Long doctorId, List<Long> antibioticDrugIds);
}
