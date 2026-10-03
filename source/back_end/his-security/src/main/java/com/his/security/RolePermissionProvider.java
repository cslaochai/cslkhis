package com.his.security;

import java.util.List;

public interface RolePermissionProvider {

    /**
     * 取该角色的权限码集合。
     *
     * <p>返回 <b>空集合</b> 是合法结果（该角色确实没配任何带权限码的菜单），与
     * "查询失败"必须区分：调用方只在拿到返回值时覆盖权限，异常时保留原值并记日志。
     *
     * @param roleCode 角色编码
     * @return 权限码列表，永不返回 null
     */
    List<String> permissionsOfRole(String roleCode);
}
