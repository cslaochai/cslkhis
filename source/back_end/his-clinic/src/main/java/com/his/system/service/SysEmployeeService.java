package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.EmployeeQueryDTO;
import com.his.system.dto.EmployeeUpsertDTO;
import com.his.system.vo.EmployeeVO;

import java.util.List;

public interface SysEmployeeService {

    PageResult<EmployeeVO> listPage(EmployeeQueryDTO queryDTO);

    List<EmployeeVO> selectList(EmployeeQueryDTO queryDTO);

    /**
     * 编辑表单的回显数据源，**不脱敏**：前端整对象回写时脱敏值会把真号洗成星号（不可逆数据损坏）。
     * 列表与下拉是另两个口径，只有那两个才脱敏。
     */
    EmployeeVO getInfo(Long id);

    /**
     * @return 面向用户的操作结果文案（新增/修改两条路文案不同）
     */
    String upsert(EmployeeUpsertDTO upsertDTO);

    void delete(Long id);
}
