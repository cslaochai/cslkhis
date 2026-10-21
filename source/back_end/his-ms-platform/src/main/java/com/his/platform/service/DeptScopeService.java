package com.his.platform.service;

/**
 * 科室权限内部接口：继承对外窄端口 {@link com.his.spi.service.DeptScopeService}，
 * 平台内部调用方继续按本类型注入，Bean 同时满足跨模块 spi 绑定。
 */
public interface DeptScopeService extends com.his.spi.service.DeptScopeService {
}
