package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.SensitiveMaskUtil;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.platform.dto.EmployeeQueryDTO;
import com.his.platform.dto.EmployeeUpsertDTO;
import com.his.platform.entity.SysDepartment;
import com.his.platform.entity.SysEmployee;
import com.his.platform.mapper.SysDepartmentMapper;
import com.his.platform.mapper.SysEmployeeMapper;
import com.his.platform.service.DeptScopeService;
import com.his.platform.service.EmployeePostService;
import com.his.platform.service.RedisSequenceService;
import com.his.platform.service.SysEmployeeService;
import com.his.platform.service.UserIdentityCache;
import com.his.platform.vo.EmployeePostVO;
import com.his.platform.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysEmployeeServiceImpl extends ServiceImpl<SysEmployeeMapper, SysEmployee> implements SysEmployeeService {

    private final SysEmployeeMapper sysEmployeeMapper;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final RedisSequenceService redisSequenceService;
    private final EmployeePostService employeePostService;
    private final DeptScopeService deptScopeService;
    private final UserIdentityCache userIdentityCache;

    @Override
    public PageResult<EmployeeVO> listPage(EmployeeQueryDTO queryDTO) {
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getEmpName()), SysEmployee::getEmpName, queryDTO.getEmpName())
                .eq(queryDTO.getEmpType() != null, SysEmployee::getEmpType, queryDTO.getEmpType());
        applyDeptScope(wrapper, queryDTO);
        applyStaffTypeFilter(wrapper, queryDTO);
        wrapper.orderByAsc(SysEmployee::getEmpCode);

        Page<SysEmployee> page = sysEmployeeMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        List<EmployeeVO> resultList = page.getRecords().stream()
                .map(this::convertToVO)
                .peek(this::maskSensitive)
                .toList();

        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), resultList);
    }

    @Override
    public List<EmployeeVO> selectList(EmployeeQueryDTO queryDTO) {
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getEmpName()), SysEmployee::getEmpName, queryDTO.getEmpName())
                .eq(queryDTO.getEmpType() != null, SysEmployee::getEmpType, queryDTO.getEmpType());
        if (Boolean.TRUE.equals(queryDTO.getOnlyScheduled())) {
            // 已排班场景：deptId 语义切换为「排班的出诊科室」，不再走主科室/岗位科室过滤（见 applyScheduledFilter）
            applyScheduledFilter(wrapper, queryDTO);
        } else {
            applyDeptScope(wrapper, queryDTO);
            applyStaffTypeFilter(wrapper, queryDTO);
            applyScheduleScopeFilter(wrapper, queryDTO);
        }

        // 2026-09-22：下拉/名册一律按 **主键升序**（原来是工号序）。
        // 这份列表主要给人挑人用（排班面板名册、各类医生下拉），
        // 而排班记录里存的医生标识就是这同一个主键 —— 两边统一按它排，
        // 看板上就不会出现「名册是 A 序、排班是 B 序」的错位。
        // ⚠️ 用 last() 就不要再叠 orderByAsc：两者都会生成 ORDER BY，拼成两段 ORDER BY 是语法错误。
        wrapper.last("ORDER BY id ASC");

        return sysEmployeeMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .peek(this::maskSensitive)
                .toList();
    }

    /**
     * 「已排班员工」过滤（{@code onlyScheduled=true} 时走这里，替代默认的主科室/岗位科室口径）：
     * 区间内（startDate/endDate，可单边省略）在门诊号源表 {@code biz_clinic_source}（del_flag=0）
     * 存在排班记录的员工才返回。
     *
     * <p>与默认口径的两点区别：
     * <ul>
     *   <li>deptId 不再过滤员工主科室，而是过滤<b>排班的出诊科室</b>（cs.dept_id）——医生在哪个
     *       科室出诊是排班事实，与档案主科室无关（多点执业在同一表里各占一条排班）；</li>
     *   <li>数据权限按「授权科室内的排班」收口：传 deptId 先过
     *       {@link DeptScopeService#resolveDeptId}（越权科室直接报错）；没传 deptId 收口到
     *       {@code allowedDeptIds()}（全院角色物化为全科室集合，IN 条件不改变结果）。</li>
     * </ul>
     *
     * <p>日期是受控的 {@link LocalDate}，{@code toString()} 恒为 yyyy-MM-dd，直接拼 SQL 无注入面。
     */
    private void applyScheduledFilter(LambdaQueryWrapper<SysEmployee> wrapper, EmployeeQueryDTO queryDTO) {
        StringBuilder sql = new StringBuilder(256)
                .append("SELECT 1 FROM biz_clinic_source cs WHERE cs.doctor_id = sys_employee.id AND cs.del_flag = 0");
        if (queryDTO.getStartDate() != null || queryDTO.getEndDate() != null) {
            LocalDate start = queryDTO.getStartDate() != null ? queryDTO.getStartDate() : LocalDate.of(1900, 1, 1);
            LocalDate end = queryDTO.getEndDate() != null ? queryDTO.getEndDate() : LocalDate.of(9999, 12, 31);
            sql.append(" AND cs.schedule_date BETWEEN '").append(start).append("' AND '").append(end).append("'");
        }
        if (queryDTO.getDeptId() != null) {
            // 受限岗位传越权科室直接报错；全院角色（allowedDeptIds 物化为全集）返回原值
            deptScopeService.resolveDeptId(queryDTO.getDeptId());
            sql.append(" AND cs.dept_id = ").append(queryDTO.getDeptId());
            wrapper.exists(sql.toString());
            return;
        }
        Set<Long> allowed = deptScopeService.allowedDeptIds();
        if (allowed.isEmpty()) {
            throw new BusinessException("当前岗位未绑定任何科室，无法查看员工数据（请在系统管理为岗位分配科室）");
        }
        String deptIds = allowed.stream().map(String::valueOf).collect(Collectors.joining(","));
        sql.append(" AND cs.dept_id IN (").append(deptIds).append(")");
        wrapper.exists(sql.toString());
    }

    @Override
    public EmployeeVO getInfo(Long id) {
        SysEmployee emp = sysEmployeeMapper.selectById(id);
        EmployeeVO vo = convertToVO(emp);
        // 详情回显才带签名笔迹（base64 大字段）；列表/下拉的 convertToVO 不填它
        if (vo != null) {
            vo.setSignImage(emp.getSignImage());
        }
        return vo;
    }

    @Override
    public EmployeeVO currentInfo() {
        Long employeeId = UserUtils.getCurrentUser().getEmployeeId();
        if (employeeId == null) {
            return null;
        }
        return getInfo(employeeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String upsert(EmployeeUpsertDTO upsertDTO) {
        SysEmployee emp = new SysEmployee();
        emp.setEmpName(upsertDTO.getEmpName());
        emp.setEmpType(upsertDTO.getEmpType());
        emp.setGender(upsertDTO.getGender());
        emp.setBirthDate(upsertDTO.getBirthDate());
        emp.setHireDate(upsertDTO.getHireDate());
        emp.setIdCard(upsertDTO.getIdCard());
        emp.setPhone(upsertDTO.getPhone());
        emp.setEmail(upsertDTO.getEmail());
        emp.setTitle(upsertDTO.getTitle());
        emp.setPosition(upsertDTO.getPosition());
        emp.setSpecialty(upsertDTO.getSpecialty());
        emp.setEducation(upsertDTO.getEducation());
        emp.setAvatar(upsertDTO.getAvatar());
        emp.setSignImage(upsertDTO.getSignImage());
        emp.setIsExpert(upsertDTO.getIsExpert());
        emp.setExpertPrice(upsertDTO.getExpertPrice());
        emp.setStatus(upsertDTO.getStatus());
        // 主科室快照两个字段不从入参写：它们由 replacePosts 按主岗位回填。
        // MyBatis-Plus 的 updateById 跳过 null 字段，所以未提交岗位的部分更新（如名册切换启用状态）
        // 也不会把主科室洗掉。

        Long empId = upsertDTO.getId();
        boolean isNew = empId == null;
        if (isNew) {
            emp.setEmpCode(redisSequenceService.generateEmployeeNo());
            sysEmployeeMapper.insert(emp);
            empId = emp.getId();
        } else {
            emp.setId(empId);
            sysEmployeeMapper.updateById(emp);
        }

        // 岗位（角色 × 科室）整体替换：鉴权直接 join 岗位表，主科室由主岗位回填。
        // posts=null 表示本次不带岗位字段，原样保留。
        employeePostService.replacePosts(empId, upsertDTO.getPosts());

        // 员工资料（姓名/科室等）也在身份缓存里；posts=null 的部分更新（如名册切启用状态）
        // 不会走 replacePosts 的失效分支，这里无条件清一次，确保改完立即生效。
        userIdentityCache.invalidateByEmployeeId(empId);

        return isNew ? "新增成功" : "修改成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        sysEmployeeMapper.deleteById(id);
        // 岗位一并清掉（显式空集合 = 清空，见 replacePosts 的 null/空语义）
        employeePostService.replacePosts(id, List.of());
    }

    /**
     * 员工数据权限收口（listPage / selectList 下拉共用）。
     *
     * <p>全院角色（data_scope=1）不收口；受限岗位：
     * <ul>
     *   <li>传了 deptId → 先过 {@link DeptScopeService#resolveDeptId}，越权科室直接报错
     *       （不静默改写），科室过滤本身仍由 {@link #applyStaffTypeFilter} 负责；</li>
     *   <li>没传 deptId → 收口到授权科室：员工主科室在授权集合内 <b>或</b> 任一岗位科室在
     *       授权集合内（OR 口径）——按岗位收是排班挑人的实际口径，主科室在 A、本科室兼职的
     *       人也要能被本科室的场次挑到，只按主科室过滤会把兼职人员漏掉。</li>
     * </ul>
     */
    private void applyDeptScope(LambdaQueryWrapper<SysEmployee> wrapper, EmployeeQueryDTO queryDTO) {
        Set<Long> allowed = deptScopeService.allowedDeptIds();
        if (allowed == null) {
            return;
        }
        if (allowed.isEmpty()) {
            throw new BusinessException("当前岗位未绑定任何科室，无法查看员工数据（请在系统管理为岗位分配科室）");
        }
        if (queryDTO.getDeptId() != null) {
            deptScopeService.resolveDeptId(queryDTO.getDeptId());
            return;
        }
        // exists 子查询里拼的是 Long 集合（无注入面），写法同 applyStaffTypeFilter 的岗位子查询
        String deptIds = allowed.stream().map(String::valueOf).collect(Collectors.joining(","));
        wrapper.and(w -> w.in(SysEmployee::getDeptId, allowed)
                .or().exists("SELECT 1 FROM sys_employee_post p WHERE p.employee_id = sys_employee.id "
                        + "AND p.dept_id IN (" + deptIds + ")"));
    }

    /**
     * 按「人事岗位类别」过滤：存在性子查询走人 × 科室 × 角色的岗位表联角色表上的岗位类别。
     *
     * <p>为什么不直接用档案上的员工类别：那是冗余分类，与岗位表不一致
     * （档案说他是护士、岗位表里他是收费员），排班挑人按岗位表为准——
     * 排错了班次比挑错人更难发现：名册里选得到，排上去才发现这个人根本不属于这个岗位。
     *
     * <p>传了 staffType 时，deptId 走<b>岗位所在科室</b>；没传时保持原行为（主科室快照），不动存量调用方。
     */
    private void applyStaffTypeFilter(LambdaQueryWrapper<SysEmployee> wrapper, EmployeeQueryDTO queryDTO) {
        if (queryDTO.getStaffType() == null) {
            wrapper.eq(queryDTO.getDeptId() != null, SysEmployee::getDeptId, queryDTO.getDeptId());
            return;
        }
        StringBuilder sb = new StringBuilder(256)
                .append("SELECT 1 FROM sys_employee_post p INNER JOIN sys_role r ON r.id = p.role_id ")
                .append("WHERE p.employee_id = sys_employee.id AND r.staff_type = ").append(queryDTO.getStaffType())
                // 岗位有效期：未到生效日的还没上岗，已过失效日的不能再排
                .append(" AND (p.effective_date IS NULL OR p.effective_date <= CURDATE())")
                .append(" AND (p.expire_date IS NULL OR p.expire_date >= CURDATE())");
        if (queryDTO.getDeptId() != null) {
            sb.append(" AND p.dept_id = ").append(queryDTO.getDeptId());
        }
        wrapper.exists(sb.toString());
    }

    /**
     * 排班域候选过滤（{@code scheduleScope} 见 {@link EmployeeQueryDTO}）：
     * <ul>
     *   <li>1-岗位出勤：排除医生/护理/护工岗（那些域有专用排班页）与急诊科室员工
     *       （主科室或任一岗位科室命中即排除）；</li>
     *   <li>2-急诊值班：只出急诊<b>叶子</b>科室的医生（虚父级不当排班单元）。</li>
     * </ul>
     * 与保存侧 StaffScheduleServiceImpl.assertScope 同一份域口径 ——
     * 候选藏人 + 保存拦截双闸，直调绕得过前者绕不过后者。
     */
    private void applyScheduleScopeFilter(LambdaQueryWrapper<SysEmployee> wrapper, EmployeeQueryDTO queryDTO) {
        Integer scope = queryDTO.getScheduleScope();
        if (scope == null) {
            return;
        }
        if (Integer.valueOf(1).equals(scope)) {
            wrapper.notExists("SELECT 1 FROM sys_employee_post p INNER JOIN sys_role r ON r.id = p.role_id "
                    + "WHERE p.employee_id = sys_employee.id AND r.staff_type IN (1, 2, 7) "
                    + "AND (p.effective_date IS NULL OR p.effective_date <= CURDATE()) "
                    + "AND (p.expire_date IS NULL OR p.expire_date >= CURDATE())");
            Set<Long> erIds = erTypeDeptIds();
            if (!erIds.isEmpty()) {
                String csv = erIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                wrapper.and(w -> w.notIn(SysEmployee::getDeptId, erIds)
                        .and(w2 -> w2.notExists("SELECT 1 FROM sys_employee_post p "
                                + "WHERE p.employee_id = sys_employee.id AND p.dept_id IN (" + csv + ")")));
            }
            return;
        }
        if (Integer.valueOf(2).equals(scope)) {
            Set<Long> leafIds = erTypeDeptIds(true);
            if (leafIds.isEmpty()) {
                wrapper.apply("1 = 0");
                return;
            }
            String csv = leafIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            wrapper.and(w -> w.in(SysEmployee::getDeptId, leafIds)
                    .or().exists("SELECT 1 FROM sys_employee_post p "
                            + "WHERE p.employee_id = sys_employee.id AND p.dept_id IN (" + csv + ")"));
            wrapper.exists("SELECT 1 FROM sys_employee_post p INNER JOIN sys_role r ON r.id = p.role_id "
                    + "WHERE p.employee_id = sys_employee.id AND r.staff_type = 1 "
                    + "AND (p.effective_date IS NULL OR p.effective_date <= CURDATE()) "
                    + "AND (p.expire_date IS NULL OR p.expire_date >= CURDATE())");
        }
    }

    /**
     * 急诊科室ID集合（dept_type 含 6，字典 his_dept_type，增量 SQL 295）。
     *
     * @param leafOnly true=只出叶子（排除被其他急诊科室指为父级的虚父级），与值班公开表同口径
     */
    private Set<Long> erTypeDeptIds(boolean leafOnly) {
        List<SysDepartment> rows = sysDepartmentMapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                .select(SysDepartment::getId, SysDepartment::getParentId)
                .eq(SysDepartment::getDelFlag, 0)
                .apply("FIND_IN_SET('6', dept_type) > 0"));
        if (rows.isEmpty()) {
            return Collections.emptySet();
        }
        if (!leafOnly) {
            return rows.stream().map(SysDepartment::getId).collect(Collectors.toSet());
        }
        Set<Long> parentIds = rows.stream().map(SysDepartment::getParentId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        return rows.stream().map(SysDepartment::getId)
                .filter(id -> !parentIds.contains(id))
                .collect(Collectors.toSet());
    }

    private Set<Long> erTypeDeptIds() {
        return erTypeDeptIds(false);
    }

    /**
     * 列表/下拉口径的敏感字段脱敏。
     * ⚠ {@link #getInfo(Long)} **不脱敏**：员工编辑表单用详情回显后再整对象写回，
     * 脱敏值 {@code 188****5678} 会被当成真实号码存库。
     */
    private void maskSensitive(EmployeeVO vo) {
        if (vo == null) {
            return;
        }
        vo.setPhone(SensitiveMaskUtil.maskPhone(vo.getPhone()));
        vo.setIdCard(SensitiveMaskUtil.maskIdCard(vo.getIdCard()));
        vo.setEmail(SensitiveMaskUtil.maskEmail(vo.getEmail()));
    }

    private EmployeeVO convertToVO(SysEmployee emp) {
        if (emp == null) {
            return null;
        }
        EmployeeVO vo = new EmployeeVO();
        vo.setId(emp.getId());
        vo.setEmpCode(emp.getEmpCode());
        vo.setEmpName(emp.getEmpName());
        vo.setEmpType(emp.getEmpType());
        vo.setGender(emp.getGender());
        vo.setBirthDate(emp.getBirthDate());
        vo.setHireDate(emp.getHireDate());
        vo.setIdCard(emp.getIdCard());
        vo.setPhone(emp.getPhone());
        vo.setEmail(emp.getEmail());
        vo.setTitle(emp.getTitle());
        vo.setPosition(emp.getPosition());
        vo.setSpecialty(emp.getSpecialty());
        vo.setEducation(emp.getEducation());
        vo.setAvatar(emp.getAvatar());
        vo.setIsExpert(emp.getIsExpert());
        vo.setExpertPrice(emp.getExpertPrice());
        vo.setStatus(emp.getStatus());
        vo.setDeptId(emp.getDeptId());
        vo.setDeptName(emp.getDeptName());

        // 岗位（角色 × 科室）是唯一的授权口径：科室清单与角色编码都从它派生，
        // 免得「档案里 4 个科室、3 个角色，但组合出来的岗位只有 2 个」两处数据各说各话。
        List<EmployeePostVO> posts = employeePostService.listPosts(emp.getId());
        vo.setPosts(posts);
        vo.setDeptIds(posts.stream().map(item -> {
            if (item.getDeptId() != null) {
                return item.getDeptId().toString();
            }
            return null;
        }).filter(Objects::nonNull).distinct().toList());
        vo.setDeptNames(posts.stream().map(EmployeePostVO::getDeptName)
                .filter(TextUtil::hasText).distinct().toList());
        vo.setRoleCodes(posts.stream().map(EmployeePostVO::getRoleCode)
                .filter(TextUtil::hasText).distinct().toList());
        return vo;
    }
}
