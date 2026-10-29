package com.his.platform.service;

import com.his.platform.support.UserIdentityCacheData;

/**
 * 用户身份（用户 + 员工 + 角色 + 权限并集）的 Redis 读缓存。
 *
 * <p>只服务 {@code JwtAuthenticationFilter} 的请求身份装配：命中后一次 Redis 读
 * 替代每次请求的 4 条 DB 查询（查用户 / 查员工 / 查角色 / 查权限并集）。
 * 登录路径不走本缓存（见 {@link UserIdentityCacheData} 为何不缓存口令）。
 */
public interface UserIdentityCache {

    String KEY_PREFIX = "his:user:identity:";

    /**
     * 命中返回身份快照；未命中或 Redis 不可用时返回 {@code null}（调用方降级走查库）。
     */
    UserIdentityCacheData get(String username);

    /**
     * 回填 / 更新缓存（查库成功后调用）。
     */
    void put(String username, UserIdentityCacheData data);

    /**
     * 失效单个用户的身份缓存（用户资料 / 状态变更后调用）。
     */
    void invalidate(String username);

    /**
     * 按员工 ID 失效其名下所有账号的身份缓存（员工资料 / 岗位变更后调用）。
     */
    void invalidateByEmployeeId(Long employeeId);

    /**
     * 全量失效（角色 / 菜单权限码变更后调用；用户数级，量小 keys 可接受）。
     */
    void invalidateAll();

    /**
     * 按角色失效其名下所有账号的身份缓存（角色菜单 / 权限变更后精准失效，替代粗暴的 invalidateAll）。
     */
    void invalidateByRoleId(Long roleId);
}
