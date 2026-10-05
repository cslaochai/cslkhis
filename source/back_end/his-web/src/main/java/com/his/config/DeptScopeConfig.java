package com.his.config;

import com.his.security.DeptScopeGuard;
import com.his.security.DeptScopeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 科室数据权限门面的装配。
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class DeptScopeConfig implements InitializingBean {

    private final DeptScopeProvider deptScopeProvider;

    @Override
    public void afterPropertiesSet() {
        DeptScopeGuard.setProvider(deptScopeProvider);
        log.info("[科室数据权限] DeptScopeProvider 已装配：{}", deptScopeProvider.getClass().getSimpleName());
    }
}
