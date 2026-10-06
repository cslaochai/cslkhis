package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.dto.DepartmentQueryDTO;
import com.his.system.dto.DepartmentSelectDTO;
import com.his.system.dto.DepartmentUpsertDTO;
import com.his.system.entity.SysDepartment;
import com.his.system.mapper.SysDepartmentMapper;
import com.his.system.service.SysDepartmentService;
import com.his.system.vo.DepartmentSelectListVO;
import com.his.system.vo.DepartmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDepartmentServiceImpl implements SysDepartmentService {
    private final DeptScopeProvider deptScopeProvider;

    /**
     * 顶级部门的固定编码：它既是树根也是「不许删、不许改父节点」的判据。
     */
    private static final String ROOT_DEPT_CODE = "1001";

    private final SysDepartmentMapper departmentMapper;

    @Override
    public List<DepartmentVO> tree() {
        List<SysDepartment> allDepts = departmentMapper.selectList(null);
        List<DepartmentVO> voList = allDepts.stream().map(this::toVO).collect(Collectors.toList());
        return buildDeptTree(voList, 0L);
    }

    @Override
    public PageResult<DepartmentVO> listPage(DepartmentQueryDTO queryDTO) {
        Page<SysDepartment> page = departmentMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), buildWrapper(queryDTO));
        List<DepartmentVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public List<DepartmentVO> list(DepartmentQueryDTO queryDTO) {
        return departmentMapper.selectList(buildWrapper(queryDTO)).stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<DepartmentSelectListVO> selectList(DepartmentSelectDTO selectDTO) {
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(selectDTO.getDeptName()), SysDepartment::getDeptName, selectDTO.getDeptName())
                .eq(selectDTO.getDeptType() != null, SysDepartment::getDeptType, selectDTO.getDeptType())
                .orderByAsc(SysDepartment::getSortOrder)
                // 唯一二级键：排序字段大量并列，缺主键兜底时列表顺序在不同请求间会漂
                .orderByAsc(SysDepartment::getId);

        if (!isAllScope(selectDTO.getScope())) {
            Set<Long> allowed = deptScopeProvider.allowedDeptIds();
            if (allowed != null) {
                if (allowed.isEmpty()) {
                    return new ArrayList<>();
                }
                wrapper.in(SysDepartment::getId, allowed);
            }
        }

        return departmentMapper.selectList(wrapper).stream().map(this::toSelectVO).collect(Collectors.toList());
    }

    @Override
    public DepartmentVO getInfo(Long deptId) {
        return toVO(departmentMapper.selectById(deptId));
    }

    @Override
    public String upsert(DepartmentUpsertDTO upsertDTO) {
        SysDepartment dept = new SysDepartment();
        BeanUtils.copyProperties(upsertDTO, dept);
        if (dept.getId() == null) {
            dept.setDeptCode(generateDeptCode(dept.getParentId()));
            departmentMapper.insert(dept);
            return "新增成功";
        }
        if (dept.getId().equals(dept.getParentId())) {
            throw new BusinessException("上级部门不能是自己");
        }

        SysDepartment existingDept = departmentMapper.selectById(dept.getId());
        if (existingDept != null && ROOT_DEPT_CODE.equals(existingDept.getDeptCode())) {
            dept.setParentId(existingDept.getParentId());
        }

        departmentMapper.updateById(dept);
        return "修改成功";
    }

    @Override
    public void delete(Long deptId) {
        SysDepartment dept = departmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        if (dept.getDeptCode() != null && ROOT_DEPT_CODE.equals(dept.getDeptCode())) {
            throw new BusinessException("顶级部门不允许删除");
        }

        Long childCount = departmentMapper.selectCount(new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getParentId, deptId));
        if (childCount > 0) {
            throw new BusinessException("该部门下有子部门，不能删除");
        }

        departmentMapper.deleteById(deptId);
    }

    private LambdaQueryWrapper<SysDepartment> buildWrapper(DepartmentQueryDTO queryDTO) {
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getDeptName()), SysDepartment::getDeptName, queryDTO.getDeptName())
                .eq(queryDTO.getDeptType() != null, SysDepartment::getDeptType, queryDTO.getDeptType())
                .orderByAsc(SysDepartment::getSortOrder);
        return wrapper;
    }

    /** scope 是否为「显式索取全部」 */
    private boolean isAllScope(String scope) {
        return "ALL".equalsIgnoreCase(scope);
    }

    private DepartmentSelectListVO toSelectVO(SysDepartment dept) {
        if (dept == null) {
            return null;
        }
        DepartmentSelectListVO vo = new DepartmentSelectListVO();
        BeanUtils.copyProperties(dept, vo);
        return vo;
    }

    private DepartmentVO toVO(SysDepartment dept) {
        if (dept == null) {
            return null;
        }
        DepartmentVO vo = new DepartmentVO();
        BeanUtils.copyProperties(dept, vo);
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
            SysDepartment parent = departmentMapper.selectById(parentId);
            if (parent == null || parent.getDeptCode() == null) {
                throw new RuntimeException("上级部门不存在");
            }
            prefix = parent.getDeptCode();
        }

        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SysDepartment::getDeptCode, prefix)
                .orderByDesc(SysDepartment::getDeptCode)
                .last("LIMIT 1");
        SysDepartment maxDept = departmentMapper.selectOne(wrapper);

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
