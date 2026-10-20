package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.*;
import com.his.medicaltech.entity.SysEmployeeTechAuth;
import com.his.medicaltech.vo.EmployeeTechAuthVO;
import com.his.medicaltech.vo.TechAuthCheckVO;
import com.his.medicaltech.vo.TechAuthOverrideVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 医疗技术临床应用授权服务
 */
public interface EmployeeTechAuthService {

    // 台账

    PageResult<EmployeeTechAuthVO> listPage(TechAuthPageQueryDTO query);

    /**
     * 取单条授权记录（含审批意见、收回原因与有效期）。
     * 供授权台账行的详情回显环节按 id 补全整条，列表行只带摘要列。
     */
    EmployeeTechAuthVO getById(Long id);

    /**
     * 按人查授权（员工档案页内嵌、医生站开单提示都用它）
     */
    List<EmployeeTechAuthVO> listByEmployee(Long employeeId);

    /**
     * 当前登录人的授权（不接收参数，工号取自 token）
     */
    List<EmployeeTechAuthVO> mine();

    /**
     * 登记或修改授权申请：新增直接落待审批，有效期不能早于生效日期，同一人同一项目的重复申请会被拦。
     * 用于「技术授权」页授权台账的新增/修改弹框，审批通过后才算生效。
     */
    void upsert(TechAuthUpsertDTO dto);

    /**
     * 委员会审批：通过即按申报级别生效，驳回带意见退回申请人，只有处于可审批状态的记录能提交。
     * 技术授权页台账行的「通过/驳回」动作。
     */
    void approve(TechAuthApproveDTO dto);

    /**
     * 收回已生效的授权（资质到期、事故调整等动态收权），只针对「已授权」的记录。
     * 技术授权页台账行的「收回」动作，收回后该人再开对应项目会被准入闸拦下。
     */
    void revoke(TechAuthRevokeDTO dto);

    /**
     * 删除授权记录，仅限待审批/已驳回（物理删）；已授权与已收回的必须留痕，只能走收回。
     * 技术授权页删掉登记错的申请行。
     */
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

    PageResult<TechAuthOverrideVO> overrideListPage(TechAuthOverridePageQueryDTO query);

    /**
     * 事后追认急诊越权：登记人本人不许确认自己那条，已确认的不许重复确认，确认人落当前登录人。
     * 用于「技术授权」页「急诊越权登记」标签页的确认动作，由上级或医务科完成。
     */
    void confirmOverride(TechAuthOverrideConfirmDTO dto);
}
