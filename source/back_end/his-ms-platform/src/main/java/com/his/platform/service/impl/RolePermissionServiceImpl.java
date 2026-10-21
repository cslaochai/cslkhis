package com.his.platform.service.impl;

import com.his.common.util.TextUtil;
import com.his.platform.service.RolePermissionService;
import com.his.platform.service.RolePermissionCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionCache rolePermissionCache;

    @Override
    public List<String> permissionsOfRole(String roleCode) {
        if (!TextUtil.hasText(roleCode)) {
            return Collections.emptyList();
        }
        List<String> permissions = rolePermissionCache.permissionsOfRole(roleCode);
        return permissions == null ? Collections.emptyList() : permissions;
    }
}
