package com.mis.service;

import com.mis.dto.DepartmentUpsertDTO;
import com.mis.entity.MssDepartment;

import java.util.List;

/**
 * 经办科室（岗位配置下拉 + HIS 契约 /system/department/**）
 */
public interface SystemDeptService {

    List<MssDepartment> selectList();

    /** 科室树（HIS 口径：/system/department/tree，按 parent_id 组装） */
    List<MssDepartment> tree();

    MssDepartment getById(Long id);

    MssDepartment upsert(DepartmentUpsertDTO dto);

    void deleteById(Long id);
}
