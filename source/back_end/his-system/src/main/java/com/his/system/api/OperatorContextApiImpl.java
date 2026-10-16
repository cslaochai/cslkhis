package com.his.system.api;

import com.his.api.service.OperatorContextApi;
import com.his.system.support.SystemOperatorContext;
import org.springframework.stereotype.Service;

/**
 * 无登录态入口操作人归属端口实现（委托 {@code SystemOperatorContext}）。
 */
@Service
public class OperatorContextApiImpl implements OperatorContextApi {

    @Override
    public void bindPatient(Long userId, Long patientId, String realName) {
        SystemOperatorContext.bindPatient(userId, patientId, realName);
    }

    @Override
    public void clear() {
        SystemOperatorContext.clear();
    }
}
