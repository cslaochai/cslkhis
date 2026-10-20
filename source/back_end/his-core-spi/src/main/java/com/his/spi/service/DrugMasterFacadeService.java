package com.his.spi.service;

import com.his.spi.vo.DrugBriefVO;

import java.util.Collection;
import java.util.List;

/**
 * 药品主数据端口：把「一发药/审方要看的那几列药品属性」按集合给出去，事实仍由药品域持有。
 *
 * <p>实现方只依赖本模块 mapper，是叶子端口：任何模块都能依赖它而不与药品域构成 Bean 环。
 */
public interface DrugMasterFacadeService {

    /**
     * 按药品ID取主数据摘要。
     *
     * <p>用于开立/审核医嘱时把处方明细上的 drugId 翻译成剂型、单价与皮试要求。
     *
     * @param drugId 药品ID
     * @return 药品摘要；药品不存在时返回 null
     */
    DrugBriefVO findDrug(Long drugId);

    /**
     * 批量取药品主数据摘要。
     *
     * <p>用于一张处方的全部明细行一次取齐，避免逐行查询退化成 N+1。
     *
     * @param drugIds 药品ID集合；为空时返回空列表
     * @return 只含实际查到的药品，顺序不保证与入参一致
     */
    List<DrugBriefVO> listDrugs(Collection<Long> drugIds);
}
