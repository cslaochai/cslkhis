package com.his.platform.service;

/**
 * 「工作台上某一张卡的数字由谁算」的提供方。
 * 内部接口：继承对外窄端口 {@link com.his.spi.service.WorkbenchMetricService}，
 * 各业务域实现由 WorkbenchServiceImpl 以 List 收集。
 */
public interface WorkbenchMetricService extends com.his.spi.service.WorkbenchMetricService {
}
