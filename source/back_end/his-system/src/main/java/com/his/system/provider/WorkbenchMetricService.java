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
     */
    Map<String, Object> summary(CurrentUser user);
}
