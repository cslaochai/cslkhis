package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.security.DeptScopeProvider;
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

/**
 * {@link DeptScopeProvider} 的实现 —— 科室数据权限的收口落地。
 *
 * <p><b>范围判定的完整口径</b>（2026-09-21 与老王定案；2026-09-25 按岗位模型收紧）：
 * <ol>
 *   <li><b>角色 data_scope=1（全部数据）→ 不收口</b>，返回 {@code null}。
 *       角色.data_scope 语义见 {@code his_data_scope} 字典：
 *       1-全部数据 2-自定义数据 3-本部门数据 4-本部门及以下 5-仅本人数据。
 *       实测 18 个角色里 8 个是 1（系统管理员 / 临床药师 / 病案编码员 / 院领导 / 审计统计员…）——
 *       这些人本来就该看全院，收口反而会把 admin 锁死在它被授权的几个科室上。</li>
 *   <li><b>其余角色 → 按「当前角色下的岗位科室」收口</b>（员工岗位，
 *       见 sql/107-岗位模型.sql）。
 *       以前是整表 distinct，于是「骨科医生 + 药房药剂师」的账号切成医生后仍能看到药房的数据 ——
 *       岗位切了、数据范围没切。按角色过滤之后，切到哪个角色就只剩那个角色的岗位科室。</li>
 *   <li><b>授权集合为空 → 回落到主科室</b>（而非返回空集）。
 *       否则新建员工还没配授权就"什么都看不见"，比不收口更难用。</li>
 * </ol>
 *
 * <p><b>已知数据现状（会造成"看起来没收口"）</b>：演示账号 {@code renyongx}
 * 在员工岗位里被授权了全部 94 个科室 —— 这是演示需要，展开成岗位后它每个角色
 * 都覆盖 94 个科室。对它的收口效果等于"看得见全部"，但**这条链路确实是通的**：
 * 换成只授权 3 个科室的账号就会立刻生效。另注：它的 {@code is_primary} 全是 0
 * （连主科室都没标），所以不能依赖 is_primary 找主科室，必须用员工的科室ID。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeptScopeProviderImpl implements DeptScopeProvider {

    private final SysEmployeePostMapper employeePostMapper;
    private final SysRoleMapper roleMapper;

    /** 全部数据 —— 不受科室范围限制 */
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
