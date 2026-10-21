package com.his.spi.service;

/**
 * 系统参数配置窄端口：跨模块读取/写入运行时系统参数。
 */
public interface SystemConfigFacadeService {

    /**
     * 读取系统参数值；key 不存在或值为空时返回 defaultValue。
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 设置系统参数值（存在则更新，不存在则插入），用于运行时修改系统参数。
     */
    void setConfigValue(String configKey, String configName, String configValue);
}
