package com.his.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 当前角色的工作台配置（外壳只认卡片 code，不认角色）。
 */
@Data
public class WorkbenchConfigVO {

    private String roleCode;

    /** 登录/切角色落点（0-默认 1-一律工作台 2-一律患者工作站），见 lib/role-workspace.js */
    private Integer landingScope;

    private List<WorkbenchWidgetVO> widgets;
}
