package com.his.system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 保存某角色的工作台配置（整表替换，与 saveRoleMenu 同口径）
 */
@Data
public class WorkbenchRoleConfigUpsertDTO {

    /** 角色ID */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 登录/切角色落点（0-默认 1-一律工作台 2-一律患者工作站） */
    @NotNull(message = "落点策略不能为空")
    private Integer landingScope;

    /** 允许为空数组=该角色清空卡片配置（首页回落到通用三张卡） */
    @Valid
    private List<WorkbenchRoleWidgetDTO> widgets;
}
