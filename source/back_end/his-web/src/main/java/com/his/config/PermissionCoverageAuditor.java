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
 *
 * <p>口径：权限码唯一来源是菜单上配置的权限标识（角色配菜单即配权限），
 * 控制器必须用 {@code @PreAuthorize} 标注。标注是手工维护的 —— 新增控制器漏标时，
 * 接口只剩「登录即可调」这道底线，权限模型会静默漂移。本扫描器在启动时把
 * <b>类级和方法级都没有 {@code @PreAuthorize}</b> 的控制器列出来打 WARN，
 * 只提示不阻断（放行清单是刻意决策的：公共码表、患者自助端等就是只要 authenticated）。
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
