package com.his.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 登录用户信息
 */
@Data
public class CurrentUser implements UserDetails {

    private Long userId;

    private String username;

    @JsonIgnore
    private String password;

    private String realName;

    private Long deptId;

    private String deptName;

    private Integer userType;

    private Long patientId;

    private Long employeeId;

    private String employeeName;

    private Integer loginCount;

    private String currentRole;

    private List<String> roles;

    private List<String> permissions;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // ROLE_ 前缀只给「当前角色」，不是全部角色：
        // 多角色账号切成收费员之后 hasRole('DOCTOR') 也必须为假，否则"切了角色但权限还是医生的"。
        // currentRole 为空（未走过滤器，例如登录认证过程中）时退回全部角色。
        if (currentRole != null && !currentRole.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + currentRole));
        } else if (roles != null) {
            roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }
        // 患者账号（用户类型为 3）：注入统一的 PATIENT authority，患者端聚合模块
        // （his-miniapp，/miniapp/**）按 hasAuthority('PATIENT') 一把抓鉴权。
        if (userType != null && userType == 3) {
            authorities.add(new SimpleGrantedAuthority("PATIENT"));
        }
        // 权限码本身不带前缀（patient:cdr:list、finance:refund:approve…），
        // 方法级鉴权写 @PreAuthorize("hasAuthority('patient:cdr:list')")。
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
