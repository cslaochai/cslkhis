package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.WorkbenchRoleConfigUpsertDTO;
import com.his.system.dto.WorkbenchWidgetPageQueryDTO;
import com.his.system.dto.WorkbenchWidgetUpsertDTO;
import com.his.system.vo.WorkbenchConfigVO;
import com.his.system.vo.WorkbenchDataVO;
import com.his.system.vo.WorkbenchRoleConfigVO;
import com.his.system.vo.WorkbenchWidgetVO;

import java.util.List;

/**
 * 门户工作台：外壳只认卡片 code，角色差异全部由配置表决定。
 */
public interface WorkbenchService {

    /**
     * 当前角色应渲染的卡片清单 + 落点策略
     */
    WorkbenchConfigVO getConfig();

    /**
     * 当前角色全部卡片的聚合数字（一次返回，逐卡 try-catch）
     */
    List<WorkbenchDataVO> getData();

    /**
     * 卡片注册表分页：按编码、名称等条件筛出可配置的全部卡片。
     * 用于系统管理「工作台卡片管理」页的查询卡与表格。
     */
    PageResult<WorkbenchWidgetVO> widgetListPage(WorkbenchWidgetPageQueryDTO queryDTO);

    /**
     * 新增或修改卡片注册项（编码、名称、所属区块、取数键与占宽），带主键即改、不带即建。
     * 用于「工作台卡片管理」页的新增与编辑弹框保存，改完首页下一次取数就按新配置走。
     */
    void widgetUpsert(WorkbenchWidgetUpsertDTO upsertDTO);

    /**
     * 删除卡片注册项，并连带清掉各角色配置里对它的引用，不留指向已删卡片的孤儿配置。
     * 用于「工作台卡片管理」页表格行的删除动作。
     */
    void widgetDelete(Long widgetId);

    /**
     * 取指定角色已勾选的卡片集合与登录落点配置。
     * 用于系统管理「角色管理」页的工作台配置弹框回显。
     */
    WorkbenchRoleConfigVO roleConfig(Long roleId);

    /**
     * 保存指定角色能看到的卡片集合与登录落点。
     * 用于同一个工作台配置弹框的保存：勾完整体落库，该角色下次进首页就是这套卡片。
     */
    void roleConfigUpsert(WorkbenchRoleConfigUpsertDTO upsertDTO);
}
