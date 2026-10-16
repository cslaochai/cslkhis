package com.his.api.service;

/**
 * JWT 签发端口（his-system 实现）。
 *
 * <p>所有签发点必须走这一个方法（登录 / 切角色 / 切科室），miniapp 不依赖 his-system 的 {@code JwtUtils}。
 */
public interface JwtProvider {

    /**
     * 生成 JWT Token。
     *
     * @param userId       用户ID
     * @param username     用户名
     * @param currentRole  当前岗位角色编码（交给权限收敛用）
     * @param deptId       当前科室ID（员工主科室或用户切换后的科室）
     * @param deptName     当前科室名称
     * @return 签名后的 token
     */
    String generateToken(Long userId, String username, String currentRole, Long deptId, String deptName);
}
