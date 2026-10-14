package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageResult;
import com.mis.common.exception.BusinessException;
import com.mis.config.LoginUser;
import com.mis.config.UserContext;
import com.mis.dto.EmployeeQueryPageDTO;
import com.mis.dto.EmployeeUpsertDTO;
import com.mis.dto.PostItemDTO;
import com.mis.dto.QualificationBatchUpsertDTO;
import com.mis.entity.MssDepartment;
import com.mis.entity.MssEmployee;
import com.mis.entity.MssEmployeePost;
import com.mis.entity.MssEmployeeQualification;
import com.mis.mapper.MssDepartmentMapper;
import com.mis.mapper.MssEmployeeMapper;
import com.mis.mapper.MssEmployeePostMapper;
import com.mis.mapper.MssEmployeeQualificationMapper;
import com.mis.service.SystemEmployeeService;
import com.mis.service.SystemPostService;
import com.mis.vo.EmployeeVO;
import com.mis.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 员工管理实现（HIS 口径）
 */
@Service
@RequiredArgsConstructor
public class SystemEmployeeServiceImpl implements SystemEmployeeService {

    private final MssEmployeeMapper employeeMapper;
    private final MssEmployeePostMapper employeePostMapper;
    private final MssEmployeeQualificationMapper qualificationMapper;
    private final MssDepartmentMapper departmentMapper;
    private final SystemPostService postService;

    @Override
    public PageResult<EmployeeVO> page(EmployeeQueryPageDTO dto) {
        Page<MssEmployee> page = employeeMapper.selectPage(new Page<>(dto.getPage(), dto.getSize()),
                new LambdaQueryWrapper<MssEmployee>()
                        .like(dto.getEmpName() != null && !dto.getEmpName().isEmpty(),
                                MssEmployee::getEmpName, dto.getEmpName())
                        .eq(dto.getStatus() != null, MssEmployee::getStatus, dto.getStatus())
                        .orderByDesc(MssEmployee::getCreateTime));

        List<EmployeeVO> vos = page.getRecords().stream().map(this::toListVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    public EmployeeVO detail(Long id) {
        MssEmployee emp = employeeMapper.selectById(id);
        if (emp == null) {
            throw new BusinessException("员工不存在");
        }
        EmployeeVO vo = toVO(emp);
        // 详情明文：编辑回显原样写回，避免把打码值洗进库
        vo.setPosts(postsOfEmployee(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MssEmployee upsert(EmployeeUpsertDTO dto) {
        String operator = operatorName();
        MssEmployee emp = new MssEmployee();
        BeanUtils.copyProperties(dto, emp);
        // 工号不可由前端提交（HIS 口径：系统自动生成）
        emp.setEmpCode(null);

        if (dto.getId() == null) {
            emp.setEmpCode(generateEmpCode());
            emp.setCreateBy(operator);
            employeeMapper.insert(emp);
        } else {
            MssEmployee exist = employeeMapper.selectById(dto.getId());
            if (exist == null) {
                throw new BusinessException("员工不存在");
            }
            emp.setId(dto.getId());
            emp.setUpdateBy(operator);
            employeeMapper.updateById(emp);
        }

        // 主岗位科室回写 dept_id / dept_name 快照（HIS：员工主科室 = 人事主科室）
        Long primaryDeptId = primaryDeptId(dto.getPosts());
        if (primaryDeptId != null) {
            MssEmployee deptUpdate = new MssEmployee();
            deptUpdate.setId(emp.getId());
            deptUpdate.setDeptId(primaryDeptId);
            deptUpdate.setDeptName(deptNameOf(primaryDeptId));
            employeeMapper.updateById(deptUpdate);
        }

        // 岗位整单替换（posts 为 null 表示未提交不处理）
        if (dto.getPosts() != null) {
            postService.replacePostsOfEmployee(emp.getId(), dto.getPosts(), operator);
        }
        return emp;
    }

    @Override
    public void deleteById(Long id) {
        MssEmployee emp = employeeMapper.selectById(id);
        if (emp == null) {
            throw new BusinessException("员工不存在");
        }
        employeeMapper.deleteById(id);
    }

    @Override
    public List<MssEmployeeQualification> qualificationList(Long employeeId) {
        return qualificationMapper.selectList(new LambdaQueryWrapper<MssEmployeeQualification>()
                .eq(MssEmployeeQualification::getEmployeeId, employeeId)
                .orderByAsc(MssEmployeeQualification::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void qualificationBatchUpsert(QualificationBatchUpsertDTO dto) {
        String operator = operatorName();
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return;
        }
        for (QualificationBatchUpsertDTO.QualificationItemDTO item : dto.getItems()) {
            MssEmployeeQualification row = new MssEmployeeQualification();
            row.setEmployeeId(dto.getEmployeeId());
            row.setCertType(item.getCertType());
            row.setCertNo(item.getCertNo());
            row.setIssueOrg(item.getIssueOrg());
            row.setIssueDate(item.getIssueDate() == null || item.getIssueDate().isEmpty()
                    ? null : LocalDate.parse(item.getIssueDate()));
            row.setValidUntil(item.getValidUntil() == null || item.getValidUntil().isEmpty()
                    ? null : LocalDate.parse(item.getValidUntil()));
            row.setRemark(item.getRemark());
            if (item.getId() == null) {
                row.setCreateBy(operator);
                qualificationMapper.insert(row);
            } else {
                row.setId(item.getId());
                row.setUpdateBy(operator);
                qualificationMapper.updateById(row);
            }
        }
    }

    @Override
    public void qualificationDelete(Long id) {
        MssEmployeeQualification row = qualificationMapper.selectById(id);
        if (row == null) {
            throw new BusinessException("证书不存在");
        }
        qualificationMapper.deleteById(id);
    }

    // ------------------------------------------------------------ 内部工具

    /** 列表出参：身份证打码（前4后4，中间星号），详情接口不走这里 */
    private EmployeeVO toListVO(MssEmployee emp) {
        EmployeeVO vo = toVO(emp);
        if (vo.getIdCard() != null && vo.getIdCard().length() >= 8) {
            String id = vo.getIdCard();
            vo.setIdCard(id.substring(0, 4) + "**********" + id.substring(id.length() - 4));
        } else {
            vo.setIdCard(null);
        }
        return vo;
    }

    private EmployeeVO toVO(MssEmployee emp) {
        EmployeeVO vo = new EmployeeVO();
        BeanUtils.copyProperties(emp, vo);
        vo.setDeptNames(deptNamesOf(emp.getId()));
        return vo;
    }

    /** 执业科室（所有岗位科室名） */
    private List<String> deptNamesOf(Long employeeId) {
        if (employeeId == null) {
            return new ArrayList<>();
        }
        return employeePostMapper.selectList(new LambdaQueryWrapper<MssEmployeePost>()
                        .eq(MssEmployeePost::getEmployeeId, employeeId))
                .stream()
                .map(MssEmployeePost::getDeptId)
                .filter(Objects::nonNull)
                .distinct()
                .map(this::deptNameOf)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<PostVO> postsOfEmployee(Long employeeId) {
        return postService.postsOfEmployee(employeeId);
    }

    private Long primaryDeptId(List<PostItemDTO> posts) {
        if (posts == null || posts.isEmpty()) {
            return null;
        }
        return posts.stream()
                .filter(p -> p.getDeptId() != null && Objects.equals(p.getIsPrimary(), 1))
                .map(PostItemDTO::getDeptId)
                .findFirst()
                .orElse(posts.stream()
                        .map(PostItemDTO::getDeptId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null));
    }

    private String deptNameOf(Long deptId) {
        if (deptId == null) return null;
        MssDepartment dept = departmentMapper.selectById(deptId);
        return dept == null ? null : dept.getDeptName();
    }

    private String generateEmpCode() {
        long max = employeeMapper.selectCount(new LambdaQueryWrapper<>());
        String code;
        do {
            code = "EMP" + String.format("%04d", ++max);
        } while (employeeMapper.selectCount(new LambdaQueryWrapper<MssEmployee>()
                .eq(MssEmployee::getEmpCode, code)) > 0);
        return code;
    }

    private String operatorName() {
        LoginUser loginUser = UserContext.get();
        return loginUser == null ? "system" : loginUser.getUsername();
    }
}
