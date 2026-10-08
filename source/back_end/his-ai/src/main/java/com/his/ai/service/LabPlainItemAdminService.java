package com.his.ai.service;

import com.his.ai.dto.LabPlainItemSearchDTO;
import com.his.ai.dto.LabPlainItemUpsertDTO;
import com.his.ai.vo.LabPlainCoverageVO;
import com.his.ai.vo.LabPlainItemAdminVO;
import com.his.common.base.PageResult;

import java.util.List;

/**
 * 检验项目白话词典 · 院内维护。
 */
public interface LabPlainItemAdminService {

    /**
     * 分页列表（含停用条目）。
     */
    PageResult<LabPlainItemAdminVO> adminPage(LabPlainItemSearchDTO dto);

    /**
     * 现有分组清单（维护页筛选下拉）。分组随词条增删变化，不算字典。
     */
    List<String> selectGroupNames();

    /**
     * 详情。
     */
    LabPlainItemAdminVO adminGetById(Long id);

    /**
     * 新增或修改，返回主键。项目名重复直接抛业务异常（不让数据库撞唯一键炸成 500）。
     */
    String adminUpsert(LabPlainItemUpsertDTO dto);

    /**
     * 物理删除（uk_item_name 不含 del_flag）。
     */
    void adminDelete(Long id);

    /**
     * 覆盖率自检：库内出现过的检验项目名，有多少没配白话。
     */
    LabPlainCoverageVO coverage();
}
