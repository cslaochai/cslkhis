package com.his.spi.service;

import com.his.spi.dto.DrugRationalCheckItemDTO;
import com.his.spi.dto.DrugRationalGroupDTO;
import com.his.spi.vo.DrugRationalCheckHitVO;
import com.his.spi.vo.DrugRationalGroupVO;

import java.util.List;
import java.util.Map;

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

    /**
     * 按处方分组的合理用药审查：每组独立判定，返回按 groupId 聚合的组级阻断结论。
     *
     * <p>扁平 {@link #check} 只摊命中，缺「这张方子整体能不能过」的组级结论；
     * 本方法额外给出本组 blocked/blockMessage 与全部 hits，供审方列表逐张处方标注。
     *
     * @param groups 分组入参（一组 = 一张处方）；为空返回空 Map
     * @return groupId → 组级审查结果；无命中的组 blocked=false、hits 为空列表
     */
    Map<Long, DrugRationalGroupVO> checkGroups(List<DrugRationalGroupDTO> groups);
}
