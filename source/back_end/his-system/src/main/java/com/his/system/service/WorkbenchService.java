package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.WorkbenchRoleConfigUpsertDTO;
import com.his.system.dto.WorkbenchWidgetQueryPageDTO;
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

    /** 当前角色应渲染的卡片清单 + 落点策略 */
    WorkbenchConfigVO getConfig();

    /** 当前角色全部卡片的聚合数字（一次返回，逐卡 try-catch） */
    List<WorkbenchDataVO> getData();

    PageResult<WorkbenchWidgetVO> widgetListPage(WorkbenchWidgetQueryPageDTO queryDTO);

    void widgetUpsert(WorkbenchWidgetUpsertDTO upsertDTO);

    void widgetDelete(Long widgetId);

    WorkbenchRoleConfigVO roleConfig(Long roleId);

    void roleConfigUpsert(WorkbenchRoleConfigUpsertDTO upsertDTO);
}
