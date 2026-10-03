package com.his.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色工作台配置里的单张卡片（顺序 + 显隐）
 */
@Data
public class WorkbenchRoleWidgetDTO {

    /** 卡片ID */
    @NotNull(message = "卡片ID不能为空")
    private Long widgetId;

    /** 该角色下的卡片顺序 */
    @NotNull(message = "排序号不能为空")
    private Integer sortOrder;

    /** 1-展示 0-关掉但不删配置行 */
    @NotNull(message = "显示状态不能为空")
    private Integer visible;
}
