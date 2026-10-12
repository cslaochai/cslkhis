package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.enums.UserTypeEnum;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysEmployee;
import com.his.system.entity.SysUser;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.mapper.SysUserMapper;
import com.his.system.service.UserIdentityCache;
import com.his.system.support.UserIdentityCacheData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    private final SysEmployeeMapper sysEmployeeMapper;

    private final UserIdentityCache userIdentityCache;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserName, username);
        SysUser user = sysUserMapper.selectOne(wrapper);

        if (user == null || user.getStatus() != EnableStatusEnum.ENABLED.getCode()) {
            throw new UsernameNotFoundException("用户不存在或者已被禁用: " + username);
        }

        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(user.getId());
        currentUser.setUsername(user.getUserName());
        currentUser.setPassword(user.getPassword());
        currentUser.setRealName(user.getRealName());
        currentUser.setUserType(user.getUserType());
        currentUser.setPatientId(user.getPatientId());
        currentUser.setEmployeeId(user.getEmpId());
        currentUser.setLoginCount(user.getLoginCount());
        if (UserTypeEnum.INNER.getCode() == user.getUserType() && user.getEmpId() != null) {
            // 院内员工
            SysEmployee employee = sysEmployeeMapper.selectById(user.getEmpId());
            if (Objects.isNull(employee)) {
                throw new UsernameNotFoundException("员工数据不存在: " + username);
            }
            currentUser.setEmployeeName(employee.getEmpName());
            // 通过员工ID查询角色
            List<String> empRoles = sysUserMapper.selectRolesByEmployeeId(user.getEmpId());
            // 通过员工ID查询权限
            List<String> permissions = sysUserMapper.selectPermissionsByEmployeeId(user.getEmpId());
            // 获取员工的主科室信息
            currentUser.setDeptId(employee.getDeptId());
            currentUser.setDeptName(employee.getDeptName());
            currentUser.setRoles(empRoles);
            currentUser.setPermissions(permissions);
        } else if (UserTypeEnum.PATIENT.getCode() == user.getUserType()) {
            // 患者用户：授予患者角色，绑定患者主档ID（用于患者级数据查询）
            currentUser.setPatientId(user.getPatientId());
            currentUser.setRoles(Collections.singletonList("PATIENT"));
        } else {
            // 院外用户等：暂无角色，登录后无权限（保持原行为）
            throw new UsernameNotFoundException("暂只支持能院内用户和患者登录");
        }
        return currentUser;
    }

    /**
     * 请求身份装配（JwtAuthenticationFilter 专用）：优先命中 Redis 身份缓存，
     * miss 才查库并回填 —— 一次 Redis 读替代每次请求的「查用户/查员工/查角色/查权限并集」4 条 SQL。
     *
     * <p><b>登录（DaoAuthenticationProvider）仍走 {@link #loadUserByUsername} 直查库</b>：
     * 口令校验必须拿最新口令与状态，而本缓存刻意不含 password（见 {@link UserIdentityCacheData}），
     * 缓存只服务 token 已验证的请求路径。
     *
     * <p>降级语义与原来完全一致：缓存 miss / Redis 不可用 → 走 {@link #loadUserByUsername}，
     * 用户不存在或已禁用时抛 {@link UsernameNotFoundException}，过滤器照旧放行/拒绝。
     */
    public UserDetails loadCachedUserByUsername(String username) {
        UserIdentityCacheData cached = userIdentityCache.get(username);
        if (cached != null) {
            return toCurrentUser(cached);
        }
        UserDetails loaded = loadUserByUsername(username);
        if (loaded instanceof CurrentUser currentUser) {
            userIdentityCache.put(username, toCacheData(currentUser));
        }
        return loaded;
    }

    private UserIdentityCacheData toCacheData(CurrentUser currentUser) {
        UserIdentityCacheData data = new UserIdentityCacheData();
        data.setUserId(currentUser.getUserId());
        data.setUsername(currentUser.getUsername());
        data.setRealName(currentUser.getRealName());
        data.setUserType(currentUser.getUserType());
        data.setPatientId(currentUser.getPatientId());
        data.setEmployeeId(currentUser.getEmployeeId());
        data.setEmployeeName(currentUser.getEmployeeName());
        data.setDeptId(currentUser.getDeptId());
        data.setDeptName(currentUser.getDeptName());
        data.setRoles(currentUser.getRoles());
        data.setPermissions(currentUser.getPermissions());
        return data;
    }

    private CurrentUser toCurrentUser(UserIdentityCacheData data) {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(data.getUserId());
        currentUser.setUsername(data.getUsername());
        currentUser.setRealName(data.getRealName());
        currentUser.setUserType(data.getUserType());
        currentUser.setPatientId(data.getPatientId());
        currentUser.setEmployeeId(data.getEmployeeId());
        currentUser.setEmployeeName(data.getEmployeeName());
        currentUser.setDeptId(data.getDeptId());
        currentUser.setDeptName(data.getDeptName());
        currentUser.setRoles(data.getRoles());
        currentUser.setPermissions(data.getPermissions());
        // password / loginCount 不在缓存负载中：过滤器路径不校验口令，
        // currentRole / deptId 随后由过滤器按 token 覆盖，与查库路径行为一致。
        return currentUser;
    }
}
