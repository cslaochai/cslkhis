package com.his.system.service;

import java.util.List;

/**
 * 角色权限服务：把角色编码翻译成该角色可用的权限码集合，是鉴权链路取按钮级权限的唯一出口。
 */
public interface RolePermissionService {

    /**
     * 取该角色的权限码集合。
     *
     * @param roleCode 角色编码
     * @return 权限码列表，永不返回 null
     */
    List<String> permissionsOfRole(String roleCode);
}
