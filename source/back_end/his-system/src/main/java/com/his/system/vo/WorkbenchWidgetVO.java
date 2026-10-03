package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 工作台卡片（外壳渲染与配置页回显共用一份结构）。
 */
@Data
public class WorkbenchWidgetVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 前端 WIDGET_REGISTRY 的键 */
    private String widgetCode;
    /** 卡片标题 */
    private String widgetName;
    /** 归属区域 */
    private String area;
    /** 取数来源标识 */
    private String apiKey;
    /** 所需权限码，null=登录即可 */
    private String permission;
    /** 栅格占宽（24 制） */
    private Integer span;
    /** 展示顺序 */
    private Integer sortOrder;
    /** 状态（1-已上线可挂载 0-注册表先占位） */
    private Integer status;
    /** 当前角色是否已勾选（配置页用；/workbench/config 恒为 1） */
    private Integer visible;
}
