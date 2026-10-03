package com.his.system.service.impl;

import com.his.security.RolePermissionProvider;
import com.his.system.service.RolePermissionCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * {@link RolePermissionProvider} 的实现 —— 按角色编码查它被授的菜单权限码。
 *
 * <p>查询走 {@link RolePermissionCache}(Redis 读缓存 + 查库回填):每个请求的
 * JwtAuthenticationFilter 都会调一次这里,不缓存的话每请求一条 SQL 且全是重复查询。
 *
 * <p>返回空集合表示"这个角色确实没有任何带权限码的菜单"（合法状态，例如刚建好还没配的角色），
 * 调用方要按"真没权限"处理；查询异常由调用方兜底（保留原权限并记日志），不在这里吞。
 */
@Service
@RequiredArgsConstructor
public class RolePermissionProviderImpl implements RolePermissionProvider {

    private final RolePermissionCache rolePermissionCache;

    @Override
    public List<String> permissionsOfRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return Collections.emptyList();
        }
        List<String> permissions = rolePermissionCache.permissionsOfRole(roleCode);
        return permissions == null ? Collections.emptyList() : permissions;
    }
}
