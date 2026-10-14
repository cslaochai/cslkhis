package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mis.common.exception.BusinessException;
import com.mis.config.LoginUser;
import com.mis.config.UserContext;
import com.mis.dto.DepartmentUpsertDTO;
import com.mis.entity.MssDepartment;
import com.mis.mapper.MssDepartmentMapper;
import com.mis.service.SystemDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 经办科室（树形 + CRUD，HIS 口径）
 */
@Service
@RequiredArgsConstructor
public class SystemDeptServiceImpl implements SystemDeptService {

    private final MssDepartmentMapper departmentMapper;

    @Override
    public List<MssDepartment> selectList() {
        return departmentMapper.selectList(new LambdaQueryWrapper<MssDepartment>()
                .eq(MssDepartment::getStatus, 1)
                .orderByAsc(MssDepartment::getSortOrder)
                .orderByAsc(MssDepartment::getId));
    }

    @Override
    public List<MssDepartment> tree() {
        List<MssDepartment> all = departmentMapper.selectList(new LambdaQueryWrapper<MssDepartment>()
                .orderByAsc(MssDepartment::getSortOrder)
                .orderByAsc(MssDepartment::getId));
        List<MssDepartment> roots = all.stream()
                .filter(d -> d.getParentId() == null || d.getParentId() == 0)
                .collect(Collectors.toList());
        for (MssDepartment root : roots) {
            fillChildren(root, all);
        }
        return roots;
    }

    private void fillChildren(MssDepartment parent, List<MssDepartment> all) {
        List<MssDepartment> children = all.stream()
                .filter(d -> Objects.equals(d.getParentId(), parent.getId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (MssDepartment child : children) {
            fillChildren(child, all);
        }
    }

    @Override
    public MssDepartment getById(Long id) {
        MssDepartment dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    @Override
    public MssDepartment upsert(DepartmentUpsertDTO dto) {
        MssDepartment dept = new MssDepartment();
        BeanUtils.copyProperties(dto, dept);
        String operator = operatorName();

        if (dto.getId() == null) {
            if (dto.getDeptCode() == null || dto.getDeptCode().isEmpty()) {
                dept.setDeptCode(generateDeptCode());
            }
            dept.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
            dept.setCreateBy(operator);
            departmentMapper.insert(dept);
        } else {
            MssDepartment exist = departmentMapper.selectById(dto.getId());
            if (exist == null) {
                throw new BusinessException("部门不存在");
            }
            dept.setId(dto.getId());
            dept.setUpdateBy(operator);
            departmentMapper.updateById(dept);
        }
        return dept;
    }

    @Override
    public void deleteById(Long id) {
        MssDepartment dept = departmentMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        Long childCount = departmentMapper.selectCount(new LambdaQueryWrapper<MssDepartment>()
                .eq(MssDepartment::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException("该部门下有子部门，不能删除");
        }
        departmentMapper.deleteById(id);
    }

    private String generateDeptCode() {
        long max = departmentMapper.selectCount(new LambdaQueryWrapper<>());
        String code;
        do {
            code = String.format("%04d", ++max);
        } while (departmentMapper.selectCount(new LambdaQueryWrapper<MssDepartment>()
                .eq(MssDepartment::getDeptCode, code)) > 0);
        return code;
    }

    private String operatorName() {
        LoginUser loginUser = UserContext.get();
        return loginUser == null ? "system" : loginUser.getUsername();
    }
}
