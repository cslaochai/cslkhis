package com.mss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mss.entity.MssDepartment;
import com.mss.mapper.MssDepartmentMapper;
import com.mss.service.SystemDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 经办科室查询
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
}
