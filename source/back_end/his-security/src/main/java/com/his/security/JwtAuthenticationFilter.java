package com.his.security;

import com.his.security.entity.CurrentUser;
import com.his.security.provider.RolePermissionProvider;
import com.his.security.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    /**
     * 按「当前角色」重算权限的来源。用 ObjectProvider 可选注入：
     * his-security 是叶子模块（his-system 依赖它，反向不成立），实现不在时过滤器也必须能用。
     */
    private final ObjectProvider<RolePermissionProvider> rolePermissionProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            String username = jwtUtils.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 设置当前角色 + 把权限收敛到当前角色
            if (userDetails instanceof CurrentUser) {
                CurrentUser currentUser = (CurrentUser) userDetails;

                // 切角色会重新签发 token，所以 token 里的 currentRole 就是用户此刻选的身份
                String currentRole = jwtUtils.getCurrentRoleFromToken(token);
                if (!StringUtils.hasText(currentRole)) {
                    List<String> roles = currentUser.getRoles();
                    if (roles != null && !roles.isEmpty()) {
                        currentRole = roles.get(0);
                    }
                }

                if (StringUtils.hasText(currentRole)) {
                    currentUser.setCurrentRole(currentRole);
                    applyRolePermissions(currentUser, currentRole);
                }

                // 「当前科室」同理：切换科室也会重签 token。**必须在这里覆盖**，
                // 否则 UserDetailsServiceImpl 每次从库里读出来的员工主科室会把用户当场
                // 切换的选择顶掉（现象：切了科室、界面上也变了，但接口一律还按原科室算，
                // 刷新后连界面都回到原科室）。token 里没有 deptId 时保持库里的值。
                Long deptId = jwtUtils.getDeptIdFromToken(token);
                if (deptId != null) {
                    currentUser.setDeptId(deptId);
                    String deptName = jwtUtils.getDeptNameFromToken(token);
                    if (StringUtils.hasText(deptName)) {
                        currentUser.setDeptName(deptName);
                    }
                }
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 把权限集合从「员工级、全部角色并集」收敛为「当前角色」。
     *
     * <p>为什么必须做：{@code UserDetailsServiceImpl} 装的是
     * {@code selectPermissionsByEmployeeId(empId)}，一个账号绑了多个角色时它是并集。
     * 于是"医生切到收费员"之后 {@code hasAuthority('emr:records:list')} 依然通过 ——
     * 想拦的场景恰恰拦不住。不收敛的话 {@code @PreAuthorize} 等于装饰品。
     *
     * <p>降级策略（刻意选的，写清楚免得以后被当成 bug）：
     * <ul>
     *   <li>提供方不存在（例如只加载 his-security 的场景）→ 保留原集合；</li>
     *   <li>查询抛异常 → 保留原集合 + ERROR 日志。宁可基础设施故障时偏宽，
     *       也不要让医生当场所有带注解的接口 403；而且此时放宽的边界
     *       <b>仍限于"这个人本来就持有的其他角色"</b>，不是任意提权，属于可接受的降级；</li>
     *   <li>查询成功（**包括空集合**）→ 覆盖。空集合是合法结果：该角色确实没配任何权限码。</li>
     * </ul>
     */
    private void applyRolePermissions(CurrentUser currentUser, String currentRole) {
        RolePermissionProvider provider = rolePermissionProvider.getIfAvailable();
        if (provider == null) {
            return;
        }
        try {
            List<String> permissions = provider.permissionsOfRole(currentRole);
            currentUser.setPermissions(permissions == null ? Collections.emptyList() : permissions);
        } catch (Exception e) {
            log.error("按角色重算权限失败，本次沿用员工级权限并集（role={}, user={}）—— 注解鉴权结果可能偏宽",
                    currentRole, currentUser.getUsername(), e);
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
