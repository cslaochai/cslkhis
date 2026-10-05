package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作台卡片注册表实体（一行=一张可被挂到工作台上的卡）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_workbench_widget")
public class SysWorkbenchWidget extends BaseEntity {

    /**
     * 卡片编码，与 WorkbenchMetricProvider.widgetCode() 一字不差
     */
    private String widgetCode;
    /**
     * 卡片标题
     */
    private String widgetName;
    /**
     * todo-待办 notice-通知 entry-快捷入口 kpi-指标墙 domain-业务域
     */
    private String area;
    /**
     * 取数来源标识（&lt;模块&gt;.&lt;summaryKey&gt;，仅对账用）
     */
    private String apiKey;
    /**
     * 可见所需权限码，NULL=登录即可；必须是菜单里真实存在的码
     */
    private String permission;
    /**
     * 栅格占宽（24 制）
     */
    private Integer defaultSpan;
    /**
     * 展示顺序
     */
    private Integer sortOrder;
    /**
     * 1-已上线 0-注册表先占位（provider 未落地时不渲染）
     */
    private Integer status;
}
