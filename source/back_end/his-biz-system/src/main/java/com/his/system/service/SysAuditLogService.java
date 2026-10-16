package com.his.system.service;


public interface SysAuditLogService {

    void record(Long userId, String userName, String module, String operation, String targetType, Object targetId, String content, boolean success, String errorMsg);
}
