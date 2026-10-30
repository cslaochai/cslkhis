package com.his.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.his.common.enums.UserTypeEnum;
import com.his.common.util.TextUtil;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 登录身份载体，也是 SecurityContext 里 {@code Authentication#getPrincipal()} 的唯一类型：
 * 全后端「当前是谁、在哪个科室、以哪个角色操作、能碰哪些按钮」都从这一个对象取
 * （入口是 {@code UserUtils.getCurrentUser()}，取不到返回 null）。
 *
 * <p>它有两条装配路径，<b>字段集并不相同</b>，不要按「同一处 new 出来的」来理解：
 * <ul>
 *   <li>登录：{@code UserDetailsServiceImpl#loadUserByUsername} 直查库 —— 口令校验必须拿最新口令与状态；</li>
 *   <li>每个请求：{@code loadCachedUserByUsername} 优先命中身份缓存，miss 才回落到查库并回填。
 *       缓存负载刻意不含口令与登录次数，所以 {@code password}、{@code loginCount} 在请求链路上恒为 null。</li>
 * </ul>
 *
 * <p>{@code currentRole}、{@code deptId}、{@code permissions} 由 JWT 过滤器在放进上下文<b>之前</b>按 token 覆盖：
 * 岗位（角色 + 科室）是用户在顶栏切换并重新签发 token 选出来的，而库里读出来的是主科室与
 * 「员工级全部角色的权限并集」，不覆盖就会把刚选的身份当场顶掉，且权限收敛失效。
 * token 里没有科室时才沿用库里的值。
 *
 * <p>第三个写入方是无登录态入口（支付渠道异步回调）：那里没有 token，只能按「这张支付单的出码人」
 * 现造一个只带身份与姓名的实例绑进上下文，用于给收款流水和审计字段归因。
 */
@Data
public class CurrentUser implements UserDetails {

    /**
     * 账号ID 除了登录的时候能用这个字段，业务记录操作人请使用employeeId，严禁使用userId
     */
    private Long userId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 口令摘要：仅登录校验那一次有值，请求链路取自身份缓存，恒为 null
     */
    @JsonIgnore
    private String password;

    /**
     * 真实姓名，落库「操作人」列的唯一口径
     */
    private String realName;

    /**
     * 当前岗位所在科室（身份 = 角色 × 科室，科室随岗位取，不是账号的固定属性）
     */
    private Long deptId;

    private String deptName;

    /**
     * 账号类型，见 {@code UserTypeEnum}；只有患者账号会被 {@link #getAuthorities()} 授予 PATIENT
     */
    private Integer userType;

    /**
     * 患者账号绑定的就诊人ID，患者端一切数据按它收口
     */
    private Long patientId;

    /**
     * 院内员工档案ID，收款流水的收银员与日结分桶按它取（患者账号为 null）
     */
    private Long employeeId;

    private String employeeName;

    /**
     * 历史登录次数，仅登录链路读出并自增
     */
    private Integer loginCount;

    /**
     * 本次请求生效的单一角色；为空时 {@link #getAuthorities()} 退回 {@link #roles} 全集
     */
    private String currentRole;

    /**
     * 该员工持有的全部角色编码（并集，仅用于画菜单与无 currentRole 时兜底，不代表本次可用角色）
     */
    private List<String> roles;

    /**
     * 本次请求生效的权限码，已由过滤器收敛到 {@link #currentRole}；
     * 过滤器重算失败时沿用装配阶段查出的「员工级并集」并记 ERROR（刻意选择的降级，宁宽不误拦）
     */
    private List<String> permissions;

    /**
     * 授权集合 = 角色 + 权限码，{@code @PreAuthorize} 与前端按钮鉴权都读它。
     * 角色优先取 {@link #currentRole} 单条而不是 {@link #roles} 全集 —— 用全集等于让多角色账号在任何岗位上都拿到全部菜单。
     */
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (TextUtil.hasText(currentRole)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + currentRole));
        } else if (roles != null) {
            roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }
        if (userType != null && userType == UserTypeEnum.PATIENT.getCode()) {
            authorities.add(new SimpleGrantedAuthority("PATIENT"));
        }
        if (permissions != null) {
            permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        return authorities;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

}
