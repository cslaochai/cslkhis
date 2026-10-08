package com.his.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * G5 权限注解覆盖率兜底告警。
 */
@Slf4j
@Component
public class PermissionCoverageAuditor implements ApplicationRunner {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public void run(ApplicationArguments args) {
        List<String> unprotected = new ArrayList<>();
        for (var entry : requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod hm = entry.getValue();
            Class<?> beanType = hm.getBeanType();
            String cls = beanType.getSimpleName();
            if (!cls.endsWith("Controller") || unprotected.contains(cls)) {
                continue;
            }
            if (beanType.isAnnotationPresent(PreAuthorize.class)) {
                continue;
            }
            boolean methodAnnotated = false;
            for (var m : beanType.getDeclaredMethods()) {
                if (m.isAnnotationPresent(PreAuthorize.class)) {
                    methodAnnotated = true;
                    break;
                }
            }
            if (!methodAnnotated) {
                unprotected.add(cls);
            }
        }
        unprotected.sort(Comparator.naturalOrder());
        if (unprotected.isEmpty()) {
            log.info("[G5] 接口权限注解覆盖率检查：所有控制器均已标注 @PreAuthorize");
        } else {
            log.warn("[G5] 以下 {} 个控制器类级/方法级均无 @PreAuthorize，接口仅受「登录即可调」保护。"
                            + "若是公共码表/患者自助等刻意放行的请忽略；否则请按 sys_menu.permission 补标注：{}",
                    unprotected.size(), unprotected);
        }
    }
}
