package com.his.spi.service;

import com.his.spi.dto.DrgGroupFactsDTO;
import com.his.spi.vo.DrgGroupResultVO;

import java.util.List;

/**
 * DRG 入组端口：把一批病案事实交给分组域，换回每组「进没进组、进了哪一组」。
 *
 * <p>分组算法、三级目录与方案版本一律留在分组域，本端口不暴露版本概念，
 * 调用方拿到的结果按当前默认启用的方案口径解释。
 */
public interface DrgGroupFacadeService {

    /**
     * 按病案事实批量入组，结果与入参下标一一对应。
     *
     * <p>用于 BI 驾驶舱算 CMI：近 30 日出院病案一次送进来，逐份拿组码去对院内权重表。
     *
     * @param factsList 病案事实列表（空或 null 返回空列表）
     * @return 与入参等长、同序的入组结果
     */
    List<DrgGroupResultVO> group(List<DrgGroupFactsDTO> factsList);
}
