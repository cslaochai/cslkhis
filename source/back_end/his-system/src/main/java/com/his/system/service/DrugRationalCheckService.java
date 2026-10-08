package com.his.system.service;

import com.his.system.dto.DrugRationalGroupDTO;
import com.his.system.dto.DrugRationalItemDTO;
import com.his.system.vo.DrugRationalGroupVO;
import com.his.system.vo.DrugRationalHitVO;

import java.util.List;

/**
 * 合理用药审查（药物相互作用 × 剂量上限）
 */
public interface DrugRationalCheckService {

    /**
     * 审查一组药品行（通常是一张处方的全部明细）
     *
     * @param items 药品行（名称与规格取单据快照）
     * @return 命中列表，禁忌级排在最前；无命中返回空列表
     */
    List<DrugRationalHitVO> check(List<DrugRationalItemDTO> items);

    /**
     * 多组一次审（审方列表页标注整页处方用），知识表只加载一遍
     */
    List<DrugRationalGroupVO> checkGroups(List<DrugRationalGroupDTO> groups);
}
