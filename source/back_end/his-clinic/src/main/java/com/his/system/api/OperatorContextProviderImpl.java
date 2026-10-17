package com.his.system.api;

import com.his.provider.service.OperatorContextProvider;
import com.his.system.support.SystemOperatorContext;
import org.springframework.stereotype.Service;

/**
 * 无登录态入口操作人归属端口实现（委托 {@code SystemOperatorContext}）。
 */
@Service
public class OperatorContextProviderImpl implements OperatorContextProvider {

    @Override
    public void bindPatient(Long userId, Long patientId, String realName) {
        SystemOperatorContext.bindPatient(userId, patientId, realName);
    }

    @Override
    public void clear() {
        SystemOperatorContext.clear();
    }
}
