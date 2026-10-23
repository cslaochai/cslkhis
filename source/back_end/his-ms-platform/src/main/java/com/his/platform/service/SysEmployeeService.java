package com.his.platform.service;

import com.his.common.base.PageResult;
import com.his.platform.dto.EmployeeQueryDTO;
import com.his.platform.dto.EmployeeUpsertDTO;
import com.his.platform.vo.EmployeeVO;

import java.util.List;

/** 员工档案服务：医护员工名册的查询、维护与岗位联动（角色×科室）的业务收口。 */
public interface SysEmployeeService {

    /**
     * 员工档案分页：按姓名、员工类型筛选，科室条件按当前岗位的数据范围收口，敏感字段脱敏后输出。
     * 用于「员工管理」页查询卡与名册表格。
     */
    PageResult<EmployeeVO> listPage(EmployeeQueryDTO queryDTO);

    /**
     * 员工下拉候选：一次取全量不分页，结果按主键升序（与排班记录里存的员工标识同一套顺序）。
     * 用于排班面板名册与手术、麻醉、预约、值班、转诊等页面选「哪位员工」的下拉。
     *
     * @param queryDTO onlyScheduled=true 时口径切为「区间内已排班的人」，科室条件改按排班的出诊科室过滤
     */
    List<EmployeeVO> selectList(EmployeeQueryDTO queryDTO);

    /**
     * 编辑表单的回显数据源，**不脱敏**：前端整对象回写时脱敏值会把真号洗成星号（不可逆数据损坏）。
     * 列表与下拉是另两个口径，只有那两个才脱敏。
     */
    EmployeeVO getInfo(Long id);

    /**
     * 取当前登录员工的档案详情（含签名笔迹），供结诊处方签名、检查检验报告审核引用笔迹。
     * 从登录态取 employeeId，不收参数；取不到登录员工时返回 null（患者账号等场景）。
     */
    EmployeeVO currentInfo();

    /**
     * @return 面向用户的操作结果文案（新增/修改两条路文案不同）
     */
    String upsert(EmployeeUpsertDTO upsertDTO);

    /**
     * 删除员工档案，并连带清空该员工的全部岗位（角色×科室）配置。
     * 用于「员工管理」页操作列的删除动作。
     */
    void delete(Long id);
}
