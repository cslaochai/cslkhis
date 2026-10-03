package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 某角色的工作台配置回显（配置页用）。
 *
 * <p>{@code widgets} 是**注册表全量**左连该角色配置行的结果，未勾选的行也会返回
 * （{@code visible=0}、{@code sortOrder} 为注册表默认值），这样一个接口就能同时喂
 * 勾选列表与右侧预览，前端不必自己拼两份数据。
 */
@Data
public class WorkbenchRoleConfigVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;

    private String roleCode;
    private String roleName;

    /** 0-默认 1-一律工作台 2-一律患者工作站（同角色多行时取最大值兜底） */
    private Integer landingScope;

    private List<WorkbenchWidgetVO> widgets;
}
