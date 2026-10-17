package com.his.system.support;

import lombok.Data;

import java.util.List;

/**
 * 用户身份缓存负载（his:user:identity:&lt;username&gt; 的 Redis JSON 值）。
 *
 * <p><b>刻意不缓存 {@code password}</b>：口令哈希只存在于 sys_user 表，
 * 登录（DaoAuthenticationProvider 校验）永远走 DB 直查拿最新口令；
 * 本缓存只服务 {@code JwtAuthenticationFilter} 的请求身份装配，
 * 该路径 token 已验证、从不使用口令 —— 缓存里放口令等于给 Redis 泄露面多挂一份凭据。
 * 同理不缓存 loginCount 等登录期才会变的字段。
 */
@Data
public class UserIdentityCacheData {

    private Long userId;

    private String username;

    private String realName;

    private Integer userType;

    private Long patientId;

    private Long employeeId;

    private String employeeName;

    private Long deptId;

    private String deptName;

    /** 员工全部角色的并集（loadUserByUsername 口径；过滤器随后按当前角色收敛）。 */
    private List<String> roles;

    /** 员工全部角色的权限码并集（同上；降级路径保留并集 = 维持既有 fail-open 语义）。 */
    private List<String> permissions;
}
