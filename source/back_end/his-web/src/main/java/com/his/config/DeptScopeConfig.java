package com.his.config;

import com.his.security.DeptScopeGuard;
import com.his.security.DeptScopeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * 科室数据权限门面的装配。
 *
 * <p>{@link DeptScopeGuard} 是静态工具（业务 Service 在任意位置都能调，不必层层注入），
 * 所以需要在这里把 Spring 容器里的 {@link DeptScopeProvider} 交给它一次。
 *
 * <p>实现 {@link InitializingBean} 而不是在构造函数里赋值：保证容器完成依赖装配后才注入，
 * 避免 provider 尚未就绪时拿到 null 而静默退化成"不受限"。
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
