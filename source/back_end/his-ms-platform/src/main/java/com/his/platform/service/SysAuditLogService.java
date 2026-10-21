package com.his.platform.service;

/**
 * 业务操作审计服务：记下「谁、在哪个模块、对哪个对象做了什么、成没成」。
 * 内部接口：继承对外窄端口 {@link com.his.spi.service.SysAuditLogService}。
 */
public interface SysAuditLogService extends com.his.spi.service.SysAuditLogService {
}
