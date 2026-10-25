package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.DataScopeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.entity.CurrentUser;
import com.his.platform.entity.SysDepartment;
import com.his.platform.entity.SysRole;
import com.his.platform.mapper.SysDepartmentMapper;
import com.his.platform.mapper.SysEmployeePostMapper;
import com.his.platform.mapper.SysRoleMapper;
import com.his.platform.service.DeptScopeService;
import com.his.common.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
     * 非 null = 受限科室集合；data_scope=4（本部门及以下）在授权科室集合上再展开全部后代科室
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
        boolean allScope = role != null && Objects.equals(DataScopeEnum.ALL.getCode(), role.getDataScope());
        if (allScope) {
            return null;
        }
        Set<Long> authorized = deptIdsOfEmployee(user.getEmployeeId(), user.getDeptId(), user.getCurrentRole());
        if (role != null && Objects.equals(DataScopeEnum.DEPT_AND_CHILD.getCode(), role.getDataScope())) {
            return expandWithDescendants(authorized);
        }
        return authorized;
    }

    /**
     * 本部门及以下（data_scope=4）：把授权科室集合按上级关系递归展开为「自身 + 全部后代科室」。
     * 科护士长岗位挂护理片区（大科）节点，只有展开后代才能看见片区内全部病区/科室的护理单元。
     *
     * @param seedDeptIds 岗位授权的科室集合（非空）
     * @return 展开后的科室ID集合，种子恒在结果里
     */
    private Set<Long> expandWithDescendants(Set<Long> seedDeptIds) {
        List<SysDepartment> departments = sysDepartmentMapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                .select(SysDepartment::getId, SysDepartment::getParentId)
                .eq(SysDepartment::getDelFlag, 0));
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (SysDepartment department : departments) {
            Long parentId = department.getParentId();
            if (parentId != null && parentId > 0 && department.getId() != null) {
                childrenByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(department.getId());
            }
        }
        // visited 兼当环守卫：parent_id 被配错成环时 BFS 会无限转
        Set<Long> expanded = new LinkedHashSet<>();
        Deque<Long> queue = new ArrayDeque<>(seedDeptIds);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (expanded.add(current)) {
                queue.addAll(childrenByParent.getOrDefault(current, List.of()));
            }
        }
        return expanded;
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
