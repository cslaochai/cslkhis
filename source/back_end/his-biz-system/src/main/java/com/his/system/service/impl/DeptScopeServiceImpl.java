package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysDepartment;
import com.his.system.entity.SysRole;
import com.his.system.mapper.SysDepartmentMapper;
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
import java.util.stream.Collectors;

/**
 * 科室权限（失败关闭 fail-closed）
 *
 * <p>所有取不到明确授权的场景一律抛异常，绝不退化为"全院可看"：
 * 未登录、患者身份、未配角色、岗位未绑科室，全部直接拒绝；
 * 全院角色（data_scope=1）物化成 sys_department 全集返回，
 * 调用方拿到的集合永不未 null，SQL 恒有科室 IN 条件，不存在漏加条件=全表的口子。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptScopeServiceImpl implements DeptScopeService {

    private static final int DATA_SCOPE_ALL = 1;

    private final SysEmployeePostMapper sysEmployeePostMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysDepartmentMapper sysDepartmentMapper;

    @Override
    public Set<Long> deptIdsOfEmployee(Long employeeId, Long primaryDeptId, String roleCode) {
        if (employeeId == null) {
            throw new BusinessException("患者身份无权访问院内业务数据");
        }

        List<Long> authorized = TextUtil.hasText(roleCode)
                ? sysEmployeePostMapper.selectDeptIdsByRole(employeeId, roleCode)
                : List.of();

        Set<Long> deptIds = new LinkedHashSet<>(authorized);
        if (deptIds.isEmpty() && primaryDeptId != null) {
            deptIds.add(primaryDeptId);
        }
        if (deptIds.isEmpty()) {
            throw new BusinessException("当前用户未绑定任何科室数据权限，请联系管理员配置岗位");
        }
        return deptIds;
    }

    @Override
    public boolean isScoped() {
        return allowedDeptIdsForCurrentUser() != null;
    }

    @Override
    public Set<Long> allowedDeptIds() {
        Set<Long> allowed = allowedDeptIdsForCurrentUser();
        return allowed != null ? allowed : allDeptIds();
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
        if (allowed == null) {
            // 全院角色：有归属数据看归属，无归属（null 科室）数据也放行
            return true;
        }
        return deptId != null && allowed.contains(deptId);
    }

    /**
     * 取当前登录用户被授权的科室集合（政策层内部使用）。
     *
     * @return null = 全院角色（公开出口 {@link #allowedDeptIds()} 会物化成 sys_department 全集）；
     * 非 null = 受限科室集合
     */
    private Set<Long> allowedDeptIdsForCurrentUser() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("未登录，无权查看，请重新登录");
        }
        if (user.getEmployeeId() == null) {
            // 患者身份（miniapp 等）不带员工上下文，一律拒绝院内业务数据
            throw new BusinessException("患者身份无权访问院内业务数据");
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

    /**
     * sys_department 全集（未删除），全院角色的收口集合。
     */
    private Set<Long> allDeptIds() {
        return sysDepartmentMapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                        .select(SysDepartment::getId)
                        .eq(SysDepartment::getDelFlag, 0))
                .stream()
                .map(SysDepartment::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
