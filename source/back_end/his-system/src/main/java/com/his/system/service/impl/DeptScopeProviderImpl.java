package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.exception.BusinessException;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysRole;
import com.his.system.mapper.SysEmployeePostMapper;
import com.his.system.mapper.SysRoleMapper;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 科室权限
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptScopeProviderImpl implements DeptScopeProvider {

    private final SysEmployeePostMapper employeePostMapper;
    private final SysRoleMapper roleMapper;

    /**
     * 全部数据 —— 不受科室范围限制
     */
    private static final int DATA_SCOPE_ALL = 1;

    @Override
    public Set<Long> deptIdsOfEmployee(Long employeeId, Long primaryDeptId, String roleCode) {
        if (employeeId == null) {
            // 拿不到员工（例如以患者身份登录）→ 不收口，交由上层其它校验兜底
            return null;
        }

        // 角色为空（老 token / 未走过滤器）时 selectDeptIdsByRole 查不到行，
        // 与「配了角色但一个岗位都没有」走同一条兜底：退回主科室，不放开全院。
        List<Long> authorized = StringUtils.hasText(roleCode)
                ? employeePostMapper.selectDeptIdsByRole(employeeId, roleCode)
                : List.of();

        Set<Long> deptIds = new LinkedHashSet<>(authorized);
        if (deptIds.isEmpty() && primaryDeptId != null) {
            deptIds.add(primaryDeptId);
        }
        return deptIds.isEmpty() ? null : deptIds;
    }

    /**
     * 该角色是否不受科室范围限制（data_scope=1 全部数据）。
     *
     * <p>命中时连员工岗位都不用查 —— 这是让 admin / 院领导
     * 看全院的**唯一**依据，不能靠"授权科室多"来碰巧实现。
     */
    @Override
    public boolean isUnrestrictedRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return false;
        }
        SysRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode).last("LIMIT 1"));
        return role != null && role.getDataScope() != null && role.getDataScope() == DATA_SCOPE_ALL;
    }

    @Override
    public boolean isScoped() {
        return allowedDeptIdsForCurrentUser() != null;
    }

    @Override
    public Set<Long> allowedDeptIds() {
        return allowedDeptIdsForCurrentUser();
    }

    @Override
    public Long resolveDeptId(Long requestedDeptId) {
        Set<Long> allowed = allowedDeptIdsForCurrentUser();
        if (allowed == null) {
            // 不受限 → 传什么用什么
            return requestedDeptId;
        }
        if (requestedDeptId == null) {
            // 受限但没指定 → 返回 null，由调用方决定"用 allowedDeptIds() 收口"还是"只取主科室"。
            // 这里刻意不自动返回主科室：调用语义（列表要看全部授权科室 / 单条要看主科室）由业务定。
            return null;
        }
        if (!allowed.contains(requestedDeptId)) {
            throw new BusinessException("无权查看该科室的数据（科室ID " + requestedDeptId + "）");
        }
        return requestedDeptId;
    }

    @Override
    public boolean canAccessDept(Long deptId) {
        Set<Long> allowed = allowedDeptIdsForCurrentUser();
        if (allowed == null) {
            return true;
        }
        return deptId != null && allowed.contains(deptId);
    }

    /**
     * 取当前登录用户被授权的科室集合（政策层内部使用）。
     *
     * @return null = 不受限（看全院 / 未登录 / 未装配）；非 null = 受限集合
     */
    private Set<Long> allowedDeptIdsForCurrentUser() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            // 未登录 → 不受限
            return null;
        }
        // 角色 data_scope=1（全部数据）直接放开 —— 否则 admin/院领导会被锁在自己被授权的几个科室上
        if (isUnrestrictedRole(user.getCurrentRole())) {
            return null;
        }
        return deptIdsOfEmployee(user.getEmployeeId(), user.getDeptId(), user.getCurrentRole());
    }
}
