package com.his.spi.service;

import com.his.spi.dto.DrugRationalCheckItemDTO;
import com.his.spi.vo.DrugRationalCheckHitVO;

import java.util.List;

/**
 * 药品域合理用药比对的对外端口：一张处方（或一组医嘱行）的相互作用与剂量上限命中结果。
 *
 * <p>知识表怎么加载、极量怎么换算由药品域判断，调用方只做「命中 → 审核项」的翻译，
 * 绝不在本模块外再写一份匹配逻辑 —— 两份判定必然漂移成「一处说没事、另一处拒绝通过」。
 */
public interface DrugRationalCheckFacadeService {

    /**
     * 审查一组药品行。
     *
     * <p>用于处方合理性审核的硬规则层，在医生提交审方之前先把配伍与超量问题摊出来。
     *
     * @param items 药品行（名称与规格取单据快照）
     * @return 命中列表，禁忌级排在最前；无命中返回空列表
     */
    List<DrugRationalCheckHitVO> check(List<DrugRationalCheckItemDTO> items);
}
