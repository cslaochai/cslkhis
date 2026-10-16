package com.his.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.his.common.util.TextUtil;
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
        if (TextUtil.hasText(currentRole)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + currentRole));
        } else if (roles != null) {
            roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }
        if (userType != null && userType == 3) {
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
