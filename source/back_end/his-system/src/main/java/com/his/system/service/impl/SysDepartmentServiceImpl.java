package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.dto.DepartmentQueryDTO;
import com.his.system.dto.DepartmentSelectDTO;
import com.his.system.dto.DepartmentUpsertDTO;
import com.his.system.entity.SysDepartment;
import com.his.system.entity.SysEmployee;
import com.his.system.mapper.SysDepartmentMapper;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.provider.DeptScopeService;
import com.his.system.service.SysDepartmentService;
import com.his.system.vo.DepartmentSelectListVO;
import com.his.system.vo.DepartmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartment> implements SysDepartmentService {
    /**
     * 顶级部门的固定编码：它既是树根也是「不许删、不许改父节点」的判据。
     */
    private static final String ROOT_DEPT_CODE = "1001";
    private final DeptScopeService deptScopeService;
    private final SysDepartmentMapper sysDepartmentMapper;
    private final SysEmployeeMapper sysEmployeeMapper;

    @Override
    public List<DepartmentVO> tree() {
        List<SysDepartment> allDepts = sysDepartmentMapper.selectList(null);
        List<DepartmentVO> voList = allDepts.stream().map(this::toVO).collect(Collectors.toList());
        return buildDeptTree(voList, 0L);
    }

    @Override
    public PageResult<DepartmentVO> listPage(DepartmentQueryDTO queryDTO) {
        Page<SysDepartment> page = sysDepartmentMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), buildWrapper(queryDTO));
        List<DepartmentVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public List<DepartmentVO> list(DepartmentQueryDTO queryDTO) {
        return sysDepartmentMapper.selectList(buildWrapper(queryDTO)).stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<DepartmentSelectListVO> selectList(DepartmentSelectDTO selectDTO) {
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(selectDTO.getDeptName()), SysDepartment::getDeptName, selectDTO.getDeptName())
                .apply(TextUtil.hasText(selectDTO.getDeptType()), 
                       "FIND_IN_SET({0}, dept_type) > 0", selectDTO.getDeptType())
                .orderByAsc(SysDepartment::getSortOrder)
                // 唯一二级键：排序字段大量并列，缺主键兜底时列表顺序在不同请求间会漂
                .orderByAsc(SysDepartment::getId);

        if (!isAllScope(selectDTO.getScope())) {
            Set<Long> allowed = deptScopeService.allowedDeptIds();
            if (allowed != null) {
                if (allowed.isEmpty()) {
                    return new ArrayList<>();
                }
                wrapper.in(SysDepartment::getId, allowed);
            }
        }

        List<SysDepartment> depts = sysDepartmentMapper.selectList(wrapper);
        Map<Long, String> parentNameMap = buildParentNameMap(depts);
        return depts.stream().map(d -> toSelectVO(d, parentNameMap)).collect(Collectors.toList());
    }

    @Override
    public DepartmentVO getInfo(Long deptId) {
        return toVO(sysDepartmentMapper.selectById(deptId));
    }

    @Override
    public String upsert(DepartmentUpsertDTO upsertDTO) {
        SysDepartment dept = new SysDepartment();
        BeanUtils.copyProperties(upsertDTO, dept);
        if (dept.getId() == null) {
            dept.setDeptCode(generateDeptCode(dept.getParentId()));
            sysDepartmentMapper.insert(dept);
            return "新增成功";
        }
        if (dept.getId().equals(dept.getParentId())) {
            throw new BusinessException("上级部门不能是自己");
        }

        SysDepartment existingDept = sysDepartmentMapper.selectById(dept.getId());
        if (existingDept != null && ROOT_DEPT_CODE.equals(existingDept.getDeptCode())) {
            dept.setParentId(existingDept.getParentId());
        }

        sysDepartmentMapper.updateById(dept);
        return "修改成功";
    }

    @Override
    public void delete(Long deptId) {
        SysDepartment dept = sysDepartmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        if (dept.getDeptCode() != null && ROOT_DEPT_CODE.equals(dept.getDeptCode())) {
            throw new BusinessException("顶级部门不允许删除");
        }

        Long childCount = sysDepartmentMapper.selectCount(new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getParentId, deptId));
        if (childCount > 0) {
            throw new BusinessException("该部门下有子部门，不能删除");
        }

        sysDepartmentMapper.deleteById(deptId);
    }

    private LambdaQueryWrapper<SysDepartment> buildWrapper(DepartmentQueryDTO queryDTO) {
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(queryDTO.getDeptName()), SysDepartment::getDeptName, queryDTO.getDeptName())
                .apply(TextUtil.hasText(queryDTO.getDeptType()), 
                       "FIND_IN_SET({0}, dept_type) > 0", queryDTO.getDeptType())
                .orderByAsc(SysDepartment::getSortOrder);
        return wrapper;
    }

    /**
     * scope 是否为「显式索取全部」
     */
    private boolean isAllScope(String scope) {
        return "ALL".equalsIgnoreCase(scope);
    }

    private DepartmentSelectListVO toSelectVO(SysDepartment dept, Map<Long, String> parentNameMap) {
        if (dept == null) {
            return null;
        }
        DepartmentSelectListVO vo = new DepartmentSelectListVO();
        BeanUtils.copyProperties(dept, vo);
        vo.setParentDeptName(parentNameMap.get(dept.getParentId()));
        return vo;
    }

    /**
     * 一次性查出所有涉及的父级科室名称，避免 N+1。
     */
    private Map<Long, String> buildParentNameMap(List<SysDepartment> depts) {
        Set<Long> parentIds = depts.stream()
                .map(SysDepartment::getParentId)
                .filter(id -> id != null && id != 0L)
                .collect(Collectors.toSet());
        if (parentIds.isEmpty()) {
            return new HashMap<>();
        }
        List<SysDepartment> parents = sysDepartmentMapper.selectBatchIds(parentIds);
        return parents.stream().collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getDeptName, (a, b) -> a));
    }

    private DepartmentVO toVO(SysDepartment dept) {
        if (dept == null) {
            return null;
        }
        DepartmentVO vo = new DepartmentVO();
        BeanUtils.copyProperties(dept, vo);
        // 填充负责人姓名
        if (dept.getDeptLeaderId() != null) {
            SysEmployee emp = sysEmployeeMapper.selectById(dept.getDeptLeaderId());
            if (emp != null) {
                vo.setDeptLeaderName(emp.getEmpName());
            }
        }
        return vo;
    }

    private List<DepartmentVO> buildDeptTree(List<DepartmentVO> depts, Long parentId) {
        return depts.stream()
                .filter(dept -> parentId.equals(dept.getParentId()))
                .peek(dept -> dept.setChildren(buildDeptTree(depts, dept.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 自动生成科室编码：根据父节点编号往后加3位递增
     * 例如：父编码 100001 -> 子编码 100001001, 100001002, ...
     * 顶级部门（parentId=0）使用 1001 为前缀
     */
    private String generateDeptCode(Long parentId) {
        String prefix;
        if (parentId == null || parentId == 0L) {
            prefix = ROOT_DEPT_CODE;
        } else {
            SysDepartment parent = sysDepartmentMapper.selectById(parentId);
            if (parent == null || parent.getDeptCode() == null) {
                throw new RuntimeException("上级部门不存在");
            }
            prefix = parent.getDeptCode();
        }

        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SysDepartment::getDeptCode, prefix)
                .orderByDesc(SysDepartment::getDeptCode)
                .last("LIMIT 1");
        SysDepartment maxDept = sysDepartmentMapper.selectOne(wrapper);

        int nextNum = 1;
        if (maxDept != null && maxDept.getDeptCode() != null && maxDept.getDeptCode().length() > prefix.length()) {
            String suffix = maxDept.getDeptCode().substring(prefix.length());
            try {
                nextNum = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException e) {
                nextNum = 1;
            }
        }

        return prefix + String.format("%03d", nextNum);
    }
}
