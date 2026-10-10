package com.his.system.provider;

import java.util.List;

public interface RolePermissionService {

    /**
     * 取该角色的权限码集合。
     *
     * @param roleCode 角色编码
     * @return 权限码列表，永不返回 null
     */
    List<String> permissionsOfRole(String roleCode);
}
