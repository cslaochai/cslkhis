package com.his.spi.service;

import com.his.spi.dto.PatientAccountCreateDTO;
import com.his.spi.vo.ClinicRoomBriefVO;
import com.his.spi.vo.DeptBriefVO;
import com.his.spi.vo.DictItemVO;
import com.his.spi.vo.EmployeeBriefVO;
import com.his.spi.vo.EmployeePostBriefVO;
import com.his.spi.vo.Icd10BriefVO;
import com.his.spi.vo.RoleBriefVO;
import com.his.spi.vo.UserBriefVO;
import com.his.spi.vo.WardBriefVO;

import java.util.Collection;
import java.util.List;

/**
 * 系统主数据对外端口：科室 / 员工 / 病区 / 岗位 / 角色的只读取数收口，
 * 供排班、挂号、医技等模块换取名快照与在岗校验，调用方不再持有主档实体与 Mapper。
 */
public interface SystemFacadeService {

    // ---------- 科室 ----------

    /**
     * 按科室ID取科室摘要（含上级科室与三类排班审核人，供沿科室树上溯定位护理片区审核节点）。
     *
     * @param deptId 科室ID（可空，为空直接返回 null）
     * @return 不存在或已删除时返回 {@code null}，报错文案由调用方决定
     */
    DeptBriefVO findDepartment(Long deptId);

    /**
     * 启用中的科室列表，用于排班「值守点位 / 排班单元」下拉候选。
     *
     * @return 按科室排序号、ID 升序
     */
    List<DeptBriefVO> listEnabledDepartments();

    /**
     * 批量取科室摘要，用于病区列表把所属科室ID翻译成科室名。
     *
     * @param deptIds 科室ID集合（空集合直接返回空列表，不发查询）
     * @return 已删除的科室不会出现在结果里，调用方按缺行处理
     */
    List<DeptBriefVO> listDepartmentsByIds(Collection<Long> deptIds);

    /**
     * 指定员工作为全院岗位排班审核人所辖的科室列表，用于待审列表的科室收口。
     *
     * @param employeeId 员工ID
     * @return 该员工没有被指定为任何科室的排班审核人时返回空列表
     */
    List<DeptBriefVO> listDepartmentsByScheduleAuditor(Long employeeId);

    /**
     * 指定员工作为门诊排班审核人（outpatient_auditor_id）所辖的科室列表，
     * 用于门诊排班待审列表的科室收口。
     *
     * @param employeeId 员工ID
     * @return 该员工没有被指定为任何科室的门诊审核人时返回空列表
     */
    List<DeptBriefVO> listDepartmentsByOutpatientAuditor(Long employeeId);

    // ---------- 员工 ----------

    /**
     * 按员工ID取员工摘要。
     *
     * @param employeeId 员工ID（可空，为空直接返回 null）
     * @return 不存在或已删除时返回 {@code null}
     */
    EmployeeBriefVO findEmployee(Long employeeId);

    /**
     * 批量取员工摘要，用于排班与留痕列表把员工ID翻译成姓名。
     *
     * @param employeeIds 员工ID集合（空集合直接返回空列表，不发查询）
     * @return 已删除的员工不会出现在结果里，调用方按缺行处理
     */
    List<EmployeeBriefVO> listEmployeesByIds(Collection<Long> employeeIds);

    /**
     * 按姓名或工号模糊匹配员工ID，用于留痕与排班列表的值班人关键词过滤。
     *
     * @param keyword 姓名或工号片段
     * @return 无命中返回空列表
     */
    List<Long> searchEmployeeIds(String keyword);

    /**
     * 取员工预维护的签名笔迹（base64 PNG），供结诊处方、检查检验报告审核做视觉落款引用。
     *
     * @param employeeId 员工ID（可空，为空直接返回 null）
     * @return 未维护或不存在时返回 {@code null}，由调用方决定缺省行为
     */
    String getEmployeeSignImage(Long employeeId);

    // ---------- 病区 ----------

    /**
     * 按病区ID取病区摘要（含所属科室ID）。
     *
     * @param wardId 病区ID（可空，为空直接返回 null）
     * @return 不存在时返回 {@code null}
     */
    WardBriefVO findWard(Long wardId);

    /**
     * 启用中的病区列表，用于排班「值守点位 / 排班单元」下拉候选。
     *
     * @return 按病区编码、ID 升序
     */
    List<WardBriefVO> listEnabledWards();

    // ---------- 岗位与角色 ----------

    /**
     * 按岗位ID取岗位摘要。
     *
     * @param postId 岗位ID（可空，为空直接返回 null）
     * @return 不存在时返回 {@code null}
     */
    EmployeePostBriefVO findEmployeePost(Long postId);

    /**
     * 取一名员工的全部岗位，用于排班时按科室选定岗位、进而带出岗位类别。
     *
     * @param employeeId 员工ID
     * @return 无岗位返回空列表（调用方据此提示「请先配置岗位」）
     */
    List<EmployeePostBriefVO> listEmployeePosts(Long employeeId);

    /**
     * 按角色ID取角色摘要（岗位类别的唯一来源）。
     *
     * @param roleId 角色ID（可空，为空直接返回 {@code null}）
     * @return 不存在或已删除时返回 {@code null}
     */
    RoleBriefVO findRole(Long roleId);

    // ---------- 员工集合取数 ----------

    /**
     * 取某科室的全部在职员工，用于会诊邀请、交接候选等「按科室找人」场景。
     *
     * @param deptId 科室ID（可空，为空返回空列表）
     * @return 停用人不出现在结果里
     */
    List<EmployeeBriefVO> listActiveEmployeesByDept(Long deptId);

    /**
     * 取某科室中担任指定职称的在职员工，用于急诊候诊升级、危急值上报找科主任。
     *
     * @param deptId 科室ID（可空，为空返回空列表）
     * @param titles 职称码集合（取 {@code EmpTitleCode.SENIOR} 等公共集合；空集合返回空列表）
     * @return 停用人不出现在结果里
     */
    List<EmployeeBriefVO> listActiveEmployeesByTitles(Long deptId, Collection<String> titles);

    /**
     * 按姓名精确取员工，用于历史单据只有姓名文本时回捞员工ID。
     *
     * @param empName 姓名（空白时返回空列表）
     * @return 命中 0 条或多条由调用方判定能否放行，实现方不做重名裁决
     */
    List<EmployeeBriefVO> listEmployeesByName(String empName);

    // ---------- 登录账号 ----------

    /**
     * 账号名是否已被占用，用于患者自助注册前的手机号查重。
     */
    boolean accountNameExists(String userName);

    /**
     * 按账号名取账号摘要，用于配置项里填写的接收人账号名换成收件人身份。
     *
     * @param userName 登录账号名
     * @return 不存在时返回 {@code null}
     */
    UserBriefVO findUserByName(String userName);

    /**
     * 开通患者类型登录账号（账号写权归系统域，患者域只提交开户申请）。
     *
     * <p>口令由实现方加密后落库，账号重名由实现方拒绝。
     *
     * @param dto 开户入参
     * @return 新账号ID
     */
    Long createPatientAccount(PatientAccountCreateDTO dto);

    /**
     * 为指定账号绑定微信 openid（重复绑定以最新为准）。
     *
     * <p>openid 已被别的账号占用时直接拒绝，不做抢占。
     */
    void bindOpenid(Long userId, String openid);

    // ---------- 诊室 ----------

    /**
     * 按诊室ID取诊室摘要（叫号队列前缀与名称的唯一来源）。
     *
     * @param roomId 诊室ID（可空，为空直接返回 {@code null}）
     * @return 不存在或已删除时返回 {@code null}
     */
    ClinicRoomBriefVO findClinicRoom(Long roomId);

    // ---------- 参数配置 ----------

    /**
     * 按配置键取配置值，用于跨模块读取业务阈值（超时时长、兜底接收人等）。
     *
     * @param configKey 配置键（取 {@code SystemConfigKeyConst} 常量）
     * @return 未配置或值为空白时返回 {@code null}，由调用方决定缺省值
     */
    String findConfigValue(String configKey);

    // ---------- 字典 ----------

    /**
     * 按字典类型取字典项，用于跨模块做码值↔文案互译。
     *
     * @param dictType 字典类型编码（取 {@code DictTypeConst} 常量）
     * @return 无配置返回空列表
     */
    List<DictItemVO> listDictItems(String dictType);

    // ---------- ICD-10 码表 ----------

    /**
     * 按关键词检索 ICD-10 编码。
     *
     * @param keyword 编码或名称片段（空白时返回维护顺序靠前的若干条）
     * @param limit   返回条数上限
     */
    List<Icd10BriefVO> searchIcd10(String keyword, int limit);

    /**
     * 全部启用中的 ICD-10 编码，供智能预测打分与合规校验的码表封闭集合用。
     */
    List<Icd10BriefVO> listActiveIcd10();

    /**
     * 码表变更后让系统域的实现缓存立即失效。
     */
    void refreshIcd10Cache();
}
