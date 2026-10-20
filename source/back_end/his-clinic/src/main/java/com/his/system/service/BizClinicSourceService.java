package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.system.dto.ScheduleSelectQueryDTO;
import com.his.appoint.vo.StopImpactItemVO;
import com.his.common.enums.StaffScheduleSourceEnum;
import com.his.system.dto.*;
import com.his.system.entity.BizClinicSource;
import com.his.system.vo.BizClinicSourceBatchResultVO;
import com.his.system.vo.OnDutyStaffVO;
import com.his.system.vo.ScheduleDetailVO;
import com.his.system.vo.ScheduleSelectListVO;

import java.util.List;

/**
 * 门诊号源排班
 */
public interface BizClinicSourceService extends IService<BizClinicSource> {

    /**
     * 给排班 VO 批量补班次展示字段（班别 + 班次名）。
     * 排班表只存 shift_id，班别是班次的属性，读侧统一在这里补齐。
     */
    void fillShiftDisplay(List<ScheduleDetailVO> voList);

    /**
     * 排班列表（含班次派生字段），口径同 {@link #listPage}。
     */
    List<ScheduleDetailVO> listDetail(ScheduleQueryDTO queryDTO);

    /**
     * 可挂号源下拉出参，字段口径同 {@link #listDetail}。
     */
    List<ScheduleSelectListVO> selectListVO(ScheduleSelectQueryDTO scheduleQueryDTO);

    /**
     * 今日排班，科室取当前登录岗位；账号没绑科室时返回空。
     */
    List<ScheduleDetailVO> todayScheduleOfCurrentDept();

    /**
     * 新增/修改排班（合一），按 id 是否为空分流。
     */
    void scheduleUpsert(ScheduleUpsertDTO upsertDTO);

    /**
     * 批量新增排班：共用表头 × 人员 × 日期 的展开与落库全部在本方法单事务内完成，
     * 任何一条校验失败整批回滚（前端禁止循环调用单条接口，事务不可控）。
     */
    BizClinicSourceBatchResultVO batchUpsert(BizClinicSourceBatchUpsertDTO batchDTO);

    /**
     * 复刻排班：把源区间内的正常排班整体后移若干天，单事务落库；
     * 目标日期已过或与既有排班重叠的条目跳过并计数。
     */
    BizClinicSourceBatchResultVO replicate(BizClinicSourceReplicateDTO dto);

    /**
     * 查询排班列表
     */
    List<BizClinicSource> listPage(ScheduleQueryDTO queryDTO);

    /**
     * 查询号源列表（员工端，套院内科室数据权限）。
     *
     * <p>科室过滤走 {@code DeptScopeService} 收口：指定越权科室直接抛、受限岗位只查授权科室、
     * 全院角色查全部。无 employeeId（患者）调用会抛"患者身份无权访问院内业务数据"。
     */
    List<BizClinicSource> scheduleSelectList(ScheduleSelectQueryDTO scheduleQueryDTO);

    /**
     * 查询号源列表（患者端挂号目录用，<b>不走院内科室数据权限</b>）。
     *
     * <p>患者身份没有 employeeId，走 {@link #scheduleSelectList} 的 deptScope 收口会直接抛
     * "患者身份无权访问院内业务数据"——但选号挂号本就是患者授权的公开业务，应按患者显式
     * 选择的 deptId/doctorId/visitDate 直接取数，不套用院内岗位科室范围。
     */
    List<BizClinicSource> scheduleSelectListUnbounded(ScheduleSelectQueryDTO scheduleQueryDTO);

    /**
     * 新增排班
     */
    boolean addSchedule(BizClinicSource schedule);

    /**
     * 新建号源行的初值收口：本表除 del_flag 外没有列默认值，实体字段留 null 会被 MP 整个省掉列，
     * 插入即报 1364。手工新增与模板批量生成共用这一处。
     */
    void initNewScheduleRow(BizClinicSource schedule);

    /**
     * 出诊计划与出勤事实对齐（手工排班、模板批量生成共用同一条口径）。
     */
    void bindCoreSchedule(BizClinicSource schedule, StaffScheduleSourceEnum source);

    /**
     * 修改排班
     */
    boolean updateSchedule(BizClinicSource schedule);

    // ===== G-15 发布态流转（提交→审核→发布 / 调班 / 撤回）=====
    /** 提交审核：草稿 → 待审核 */
    void submit(Long id);

    /** 审核发布：待审核 → 已发布，并生成号源 */
    void auditPublish(Long id);

    /** 撤回：任意态 → 草稿，并清理号源 */
    void recall(Long id);

    /** 进入调班：已发布/已变更 → 调班中（锁定编辑，号源暂冻结） */
    void adjustStart(Long id);

    /**
     * 整周提交审核：区间内本科室草稿(0) → 待审核(1)，返回迁移条数；已发布/待审核/调班中行不动。
     * 逐条走单条提交逻辑并记留痕，单事务。
     */
    int submitWeek(ScheduleQueryDTO query);

    /**
     * 整周审核发布：区间内本科室待审核(1) → 已发布(2)，逐条补号段并同步出勤事实，返回迁移条数；
     * 草稿/调班中行不动。单事务。
     */
    int auditWeek(ScheduleQueryDTO query);

    /**
     * 删除排班
     */
    boolean deleteSchedule(Long id);

    /**
     * 查询今日本科室排班（诊室+医生+就诊状态）
     */
    List<BizClinicSource> getTodaySchedule(Long deptId);

    /**
     * 更新就诊状态（0-待开始 1-接诊中 2-暂停）
     */
    boolean updateConsultStatus(Long scheduleId, Integer consultStatus);

    /**
     * 停诊/启用（只更新状态列，不动号源与时间）
     */
    boolean updateStatus(Long scheduleId, Integer status);

    /**
     * 停诊影响名单：该班次在挂（未退号/未爽约）的挂号记录
     */
    List<StopImpactItemVO> stopImpact(Long scheduleId);

    /**
     * 停诊批量退号：逐条走真实退号（按渠道还池），返回「成功 N/共 M」结论。
     *
     * @param scheduleId 所属排班ID（可空；空则只退号、不落排班变更日志）
     */
    String batchCancel(Long scheduleId, List<Long> registIds, String reason);

    /**
     * 加号：total/available 同步 +addNum，added_source 累计留痕，原因写 remark
     */
    boolean addSource(Long scheduleId, Integer addNum, String reason);

    /**
     * <b>今日在岗</b> —— 排班的下游出口。
     */
    List<OnDutyStaffVO> onDuty(OnDutyQueryDTO queryDTO);

    /**
     * 挑一个「当班责任人」——给需要落执行人的单据用（如分诊记录上的当班护士）。
     *
     * @param deptId      科室ID
     * @param staffType   岗位类别
     * @param preferEmpId 优先取此人（当前操作人的员工ID），可为 null
     */
    OnDutyStaffVO pickDutyStaff(Long deptId, Integer staffType, Long preferEmpId);
}
