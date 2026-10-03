package com.his.system.vo;

import lombok.Data;

import java.util.Map;

/**
 * 一张卡片的聚合取数结果。
 *
 * <p>{@code error} 非空表示该卡取数失败：前端渲染「—」而不是整屏 500，
 * 因为一次 {@code /workbench/data} 里任何一张卡的 SQL 问题都不该连带其它卡。
 */
@Data
public class WorkbenchDataVO {

    /** 卡片编码（工作台卡片注册表.widget_code） */
    private String code;

    private Map<String, Object> data;

    private String error;
}
