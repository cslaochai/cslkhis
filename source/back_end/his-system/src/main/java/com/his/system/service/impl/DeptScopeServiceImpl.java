package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysRole;
import com.his.system.mapper.SysEmployeePostMapper;
import com.his.system.mapper.SysRoleMapper;
import com.his.system.provider.DeptScopeService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 科室权限
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptScopeServiceImpl implements DeptScopeService {

    private static final int DATA_SCOPE_ALL = 1;

    private final SysEmployeePostMapper sysEmployeePostMapper;

    private final SysRoleMapper sysRoleMapper;

    @Override
    public Set<Long> deptIdsOfEmployee(Long employeeId, Long primaryDeptId, String roleCode) {
        if (employeeId == null) {
            return null;
        }

        List<Long> authorized = TextUtil.hasText(roleCode)
                ? sysEmployeePostMapper.selectDeptIdsByRole(employeeId, roleCode)
                : List.of();

        Set<Long> deptIds = new LinkedHashSet<>(authorized);
        if (deptIds.isEmpty() && primaryDeptId != null) {
            deptIds.add(primaryDeptId);
        }
        return deptIds.isEmpty() ? null : deptIds;
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
            return requestedDeptId;
        }
        if (requestedDeptId == null) {
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
        return allowed != null && deptId != null && allowed.contains(deptId);
    }

    /**
     * 取当前登录用户被授权的科室集合（政策层内部使用）。
     *
     * @return null = 不受限（看全院 / 未登录 / 未装配）；非 null = 受限集合
     */
    private Set<Long> allowedDeptIdsForCurrentUser() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("未登录，无权查看，请重新登录");
        }
        if (!TextUtil.hasText(user.getCurrentRole())) {
            throw new BusinessException("当前用户未能配置正确角色");
        }
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, user.getCurrentRole()).last("LIMIT 1"));
        boolean allScope = role != null && role.getDataScope() != null && role.getDataScope() == DATA_SCOPE_ALL;
        if (allScope) {
            return null;
        }
        return deptIdsOfEmployee(user.getEmployeeId(), user.getDeptId(), user.getCurrentRole());
    }
}
