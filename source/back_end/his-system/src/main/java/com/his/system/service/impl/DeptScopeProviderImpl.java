package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.security.provider.DeptScopeProvider;
import com.his.system.entity.SysRole;
import com.his.system.mapper.SysEmployeePostMapper;
import com.his.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
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
}
