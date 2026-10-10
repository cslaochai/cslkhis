package com.his.system.provider;

import com.his.system.entity.CurrentUser;

import java.util.Map;

/**
 * 「工作台上某一张卡的数字由谁算」的提供方（SPI）。
 */
public interface WorkbenchMetricService {

    /** 卡片编码，对应工作台卡片注册表里登记的编码 */
    String widgetCode();

    /**
     * 取该卡当前登录人所见的数字。
     *
     * @param user 当前登录人（含 currentRole/deptId/employeeId），调用方保证非 null
     * @return 出参 Map；其中 Long 类型的 id 一律先转字符串再放（AGENTS.md §1 精度铁律），
     *         返回 null 与返回空 Map 等价（前端渲染「—」）
     */
    Map<String, Object> summary(CurrentUser user);
}
