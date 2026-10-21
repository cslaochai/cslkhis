package com.his.platform.vo;

import lombok.Data;

import java.util.List;

/**
 * 当前角色的工作台配置（外壳只认卡片 code，不认角色）。
 */
@Data
public class WorkbenchConfigVO {

    /** 当前登录角色的编码 */
    private String roleCode;

    /**
     * 登录/切角色落点（0-默认 1-一律工作台 2-一律患者工作站），见 lib/role-workspace.js
     */
    private Integer landingScope;

    /** 该角色要渲染的卡片集合（按展示顺序） */
    private List<WorkbenchWidgetVO> widgets;
}
