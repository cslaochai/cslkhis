package com.mss.service;

import com.mss.entity.MssDepartment;

import java.util.List;

/**
 * 经办科室（岗位配置用下拉）
 */
public interface SystemDeptService {

    List<MssDepartment> selectList();
}
