package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.SensitiveMaskUtil;
import com.his.common.util.TextUtil;
import com.his.system.dto.EmployeeQueryDTO;
import com.his.system.dto.EmployeeUpsertDTO;
import com.his.system.entity.SysEmployee;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.service.EmployeePostService;
import com.his.system.service.SysEmployeeService;
import com.his.system.vo.EmployeePostVO;
import com.his.system.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SysEmployeeServiceImpl extends ServiceImpl<SysEmployeeMapper, SysEmployee> implements SysEmployeeService {

    private final SysEmployeeMapper sysEmployeeMapper;
    private final RedisSequenceService redisSequenceService;
    private final EmployeePostService employeePostService;

    @Override
    public PageResult<EmployeeVO> listPage(EmployeeQueryDTO queryDTO) {
        LambdaQueryWrapper<SysEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getEmpName()), SysEmployee::getEmpName, queryDTO.getEmpName())
                .eq(queryDTO.getEmpType() != null, SysEmployee::getEmpType, queryDTO.getEmpType());
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
        applyStaffTypeFilter(wrapper, queryDTO);

        // 2026-09-22：下拉/名册一律按 **主键升序**（原来是工号序）。
        // 这份列表主要给人挑人用（排班面板名册、预约看板周视图的医生行、各类医生下拉），
        // 而排班记录里存的医生标识就是这同一个主键 —— 两边统一按它排，
        // 看板上就不会出现「名册是 A 序、排班是 B 序」的错位。
        // ⚠️ 用 last() 就不要再叠 orderByAsc：两者都会生成 ORDER BY，拼成两段 ORDER BY 是语法错误。
        wrapper.last("ORDER BY id ASC");

        return sysEmployeeMapper.selectList(wrapper).stream()
                .map(this::convertToVO)
                .peek(this::maskSensitive)
                .toList();
    }

    @Override
    public EmployeeVO getInfo(Long id) {
        SysEmployee emp = sysEmployeeMapper.selectById(id);
        return convertToVO(emp);
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
