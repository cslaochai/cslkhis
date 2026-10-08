package com.his.system.vo;

import lombok.Data;

import java.util.Map;

/**
 * 一张卡片的聚合取数结果。
 */
@Data
public class WorkbenchDataVO {

    /**
     * 卡片编码（工作台卡片注册表.widget_code）
     */
    private String code;

    /**
     * 该卡的指标集合，键名由该卡的 {@code WorkbenchMetricProvider} 实现决定（见类注释）
     */
    private Map<String, Object> data;

    private String error;
}
