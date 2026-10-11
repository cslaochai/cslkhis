package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.system.dto.OnDutyQueryDTO;
import com.his.system.dto.ScheduleQueryDTO;
import com.his.system.dto.ScheduleSelectQueryDTO;
import com.his.system.dto.ScheduleUpsertDTO;
import com.his.system.entity.BizClinicSource;
import com.his.system.vo.OnDutyStaffVO;
import com.his.system.vo.ScheduleDetailVO;
import com.his.system.vo.ScheduleSelectListVO;
import com.his.system.vo.StopImpactItemVO;
import com.his.common.enums.StaffScheduleSourceEnum;

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
     * 查询排班列表
     */
    List<BizClinicSource> listPage(ScheduleQueryDTO queryDTO);

    /**
     * 查询号源列表
     */
    List<BizClinicSource> scheduleSelectList(ScheduleSelectQueryDTO scheduleQueryDTO);

    /**
     * 新增排班
     */
    boolean addSchedule(BizClinicSource schedule);

    /**
     * 出诊计划与出勤事实对齐（手工排班、模板批量生成共用同一条口径）。
     */
    void bindCoreSchedule(BizClinicSource schedule, StaffScheduleSourceEnum source);

    /**
     * 修改排班
     */
    boolean updateSchedule(BizClinicSource schedule);

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
     * 停诊批量退号：逐条走真实退号（按渠道还池），返回「成功 N/共 M」结论
     */
    String batchCancel(List<Long> registIds, String reason);

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
     * @param deptId      科室ID
     * @param staffType   岗位类别
     * @param preferEmpId 优先取此人（当前操作人的员工ID），可为 null
     */
    OnDutyStaffVO pickDutyStaff(Long deptId, Integer staffType, Long preferEmpId);
}
