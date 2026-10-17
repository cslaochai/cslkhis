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

    /**
     * 登录账号主键（装配进 CurrentUser.userId，下游按它定位「当前登录人」）。
     */
    private Long userId;

    /**
     * 用户名，同时是本缓存 Redis key 的后缀；回填时以方法参数 username 为准，与此字段互为校验。
     */
    private String username;

    /**
     * 真实姓名，操作人取值的唯一来源（§20：落库的操作人只认这个字段，不回落到 username）。
     */
    private String realName;

    /**
     * 用户类型（1-院内用户 2-院外用户 3-患者 4-其他），决定下游走员工分支还是患者分支。
     */
    private Integer userType;

    /**
     * 患者主档ID（仅 userType=3 有值）：患者级数据查询的收口依据。
     */
    private Long patientId;

    /**
     * 员工档案ID（仅院内用户有值）：过滤器随后按 token 里的岗位信息收敛到具体角色 + 科室。
     */
    private Long employeeId;

    /**
     * 员工姓名（来自员工档案，可与 realName 不同）。
     */
    private String employeeName;

    /**
     * 主岗位所在科室ID，来自员工档案快照。
     * <p>token 中带了 deptId 时由过滤器覆盖成 token 口径；本字段只是「没带岗位信息」时的兜底落点。
     */
    private Long deptId;

    /**
     * 主岗位所在科室名称（与 deptId 同源，随快照一起过期，不做实时翻译）。
     */
    private String deptName;

    /** 员工全部角色的并集（loadUserByUsername 口径；过滤器随后按当前角色收敛）。 */
    private List<String> roles;

    /** 员工全部角色的权限码并集（同上；降级路径保留并集 = 维持既有 fail-open 语义）。 */
    private List<String> permissions;
}
