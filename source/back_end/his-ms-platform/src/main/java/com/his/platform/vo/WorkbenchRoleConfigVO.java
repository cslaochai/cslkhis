package com.his.platform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 某角色的工作台配置回显（配置页用）。
 */
@Data
public class WorkbenchRoleConfigVO {

    /** 角色ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roleId;

    /** 角色编码 */
    private String roleCode;
    /** 角色名称 */
    private String roleName;

    /**
     * 0-默认 1-一律工作台 2-一律患者工作站（同角色多行时取最大值兜底）
     */
    private Integer landingScope;

    /** 该角色已配置的工作台卡片列表 */
    private List<WorkbenchWidgetVO> widgets;
}
