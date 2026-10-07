package com.his.system.service;

import com.his.common.base.PageResult;
import com.his.system.dto.*;
import com.his.system.entity.SysEmployeeTechAuth;
import com.his.system.vo.EmployeeTechAuthVO;
import com.his.system.vo.TechAuthCheckVO;
import com.his.system.vo.TechAuthOverrideVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 医疗技术临床应用授权服务（sql/155）。
 */
public interface EmployeeTechAuthService {

    // 台账

    PageResult<EmployeeTechAuthVO> listPage(TechAuthQueryPageDTO query);

    EmployeeTechAuthVO getById(Long id);

    /**
     * 按人查授权（员工档案页内嵌、医生站开单提示都用它）
     */
    List<EmployeeTechAuthVO> listByEmployee(Long employeeId);

    /**
     * 当前登录人的授权（不接收参数，工号取自 token）
     */
    List<EmployeeTechAuthVO> mine();

    void upsert(TechAuthUpsertDTO dto);

    void approve(TechAuthApproveDTO dto);

    void revoke(TechAuthRevokeDTO dto);

    void deleteById(Long id);

    // 闸门（跨模块）

    /**
     * 某人在某类别上、指定日期生效的授权（取级别上限最高的那条），没有返回 null
     */
    SysEmployeeTechAuth findEffective(Long employeeId, Integer authCategory, LocalDate operateDate);

    /**
     * 只判定不拦截，供前端展示「我还差哪一级」
     */
    TechAuthCheckVO checkAuthorized(Long employeeId, Integer authCategory, Integer requiredLevel, String itemCode, LocalDate operateDate);

    /**
     * 准入闸：有权限则放行；无权限且非急诊直接抛 {@code BusinessException}；
     * 无权限但急诊则写一笔越权登记后放行。
     */
    TechAuthCheckVO gate(TechAuthGateDTO gate);

    // 越权登记

    PageResult<TechAuthOverrideVO> overrideListPage(TechAuthOverrideQueryPageDTO query);

    void confirmOverride(TechAuthOverrideConfirmDTO dto);
}
