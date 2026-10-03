package com.his.appoint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.appoint.dto.OnDutyQueryDTO;
import com.his.appoint.dto.ScheduleQueryDTO;
import com.his.appoint.dto.ScheduleSelectQueryDTO;
import com.his.appoint.dto.ScheduleUpsertDTO;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.vo.OnDutyStaffVO;
import com.his.appoint.vo.ScheduleDetailVO;
import com.his.appoint.vo.ScheduleSelectListVO;
import com.his.appoint.vo.StopImpactItemVO;
import com.his.common.enums.StaffScheduleSourceEnum;

import java.util.List;

/**
 * 挂号服务接口
 */
public interface ScheduleService extends IService<BizSchedule> {

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
    List<BizSchedule> listPage(ScheduleQueryDTO queryDTO);

    /**
     * 查询号源列表
     */
    List<BizSchedule> scheduleSelectList(ScheduleSelectQueryDTO scheduleQueryDTO);

    /**
     * 新增排班
     */
    boolean addSchedule(BizSchedule schedule);

    /**
     * 出诊计划与出勤事实对齐（手工排班、模板批量生成共用同一条口径）。
     *
     * <p>按「人 × 日 × 班」取到对应的那条全院排班事实（没有就落一条），并把投影行上的
     * 星期、岗位类别、姓名、科室名、起止时间一律换成事实层的值 —— 这五样在出诊计划上都是<b>派生列</b>，
     * 谁自己写一份就会出现「门诊表说他是医生、岗位表说他是收费员」。
     *
     * <p>方法会<b>就地改写</b>入参实体，调用方拿到的 {@code schedule} 已是可直接落库的完整投影。
     * 必须在 {@code applyShift} 之后调用（班次定了时间，事实才认这个班）。
     *
     * @param source 生成来源：手工排班落 1-手工，周模板生成落 2-模板
     */
    void bindCoreSchedule(BizSchedule schedule, StaffScheduleSourceEnum source);

    /**
     * 修改排班
     */
    boolean updateSchedule(BizSchedule schedule);

    /**
     * 删除排班
     */
    boolean deleteSchedule(Long id);

    /**
     * 查询今日本科室排班（诊室+医生+就诊状态）
     */
    List<BizSchedule> getTodaySchedule(Long deptId);

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
     *
     * <p>排班表是「计划」，业务流程真正要问的是「此刻/今天谁在岗」。
     * 医生出诊、护理出勤、收费窗口、药师值班都从这里出，按 staffType 分流。
     * 判定走 {@link com.his.common.util.ShiftCoverUtil}（跨零点夜班唯一实现）。
     */
    List<OnDutyStaffVO> onDuty(OnDutyQueryDTO queryDTO);

    /**
     * 挑一个「当班责任人」——给需要落执行人的单据用（如分诊记录上的当班护士）。
     *
     * <p>取值顺序：
     * <ol>
     *   <li>{@code preferEmpId} 本人恰在当班名单里 → 取本人（护士本人操作就该记她）；</li>
     *   <li>此刻在岗名单里第一个（班次开始时间最晚的那一班，交接班时段取接班人）；</li>
     *   <li>此刻无人（交接班空档）→ 取当日班次里最早的那一班（当天在岗过的人）；</li>
     *   <li>都没有 → 返回 null。<b>绝不拿非当班人员顶替</b>：写不出真实值就留空，
     *       比记一个「谁登的浏览器就是谁」的假数据强（历史教训：分诊护士恒等于登录人）。</li>
     * </ol>
     *
     * @param deptId      科室ID
     * @param staffType   岗位类别
     * @param preferEmpId 优先取此人（当前操作人的员工ID），可为 null
     */
    OnDutyStaffVO pickDutyStaff(Long deptId, Integer staffType, Long preferEmpId);
}
