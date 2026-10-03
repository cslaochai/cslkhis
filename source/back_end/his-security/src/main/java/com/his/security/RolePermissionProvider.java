package com.his.security;

import java.util.List;

/**
 * 「某个角色拥有哪些权限」的提供方（SPI）。
 *
 * <p>为什么需要它：{@code UserDetailsServiceImpl} 里装的 permissions 是
 * {@code selectPermissionsByEmployeeId(empId)} —— <b>员工级、全部角色的并集</b>，
 * 里面没有"当前角色"这个概念。而一个账号可以绑多个角色（演示账号 renyongx 绑了全部 18 个），
 * 于是"医生切到收费员"之后 {@code hasAuthority('emr:records:list')} 依然通过，
 * 想拦的场景恰恰拦不住。所以必须能按 <b>当前角色</b> 单独取一次权限。
 *
 * <p>放在 his-security（叶子模块）而不是 his-system：过滤器在这里，
 * 而 his-system 依赖 his-security，反向依赖会成环。实现由 his-system 提供
 * （见 {@code RolePermissionProviderImpl}）。
 *
 * <p>口径：权限码复用菜单上配置的权限标识，角色配菜单即配权限 ——
 * 不另建"角色→权限"映射表（手工维护的映射表一定会和菜单配置漂移）。
 */
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
