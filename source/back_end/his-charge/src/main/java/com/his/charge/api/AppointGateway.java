package com.his.charge.api;

import com.his.charge.support.ChargeDeptResolver;
import com.his.charge.vo.RegistBriefVO;

/**
 * 挂号域对收费域提供的只读端口（依赖倒置）。
 */
public interface AppointGateway {

    /**
     * 按挂号ID取挂号摘要。
     *
     * @param registId 挂号ID（可空，为空直接返回 null）
     * @return 不存在时返回 {@code null}
     */
    RegistBriefVO findRegist(Long registId);

    /**
     * 按挂号单号反查开单科室（收费明细的科室归属反查入口）。
     *
     * @param registNo 挂号单号
     * @return 查不到返回 {@code null}（调用方保持科室为空，<b>不得兜底成默认科室</b>）
     */
    ChargeDeptResolver.DeptRef findDeptByRegistNo(String registNo);
}
