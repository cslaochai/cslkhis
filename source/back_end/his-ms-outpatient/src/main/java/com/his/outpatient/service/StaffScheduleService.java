package com.his.outpatient.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.common.enums.StaffScheduleSourceEnum;
import com.his.outpatient.dto.StaffScheduleCheckDTO;
import com.his.outpatient.dto.StaffScheduleCopyDTO;
import com.his.outpatient.dto.StaffSchedulePageQueryDTO;
import com.his.outpatient.dto.StaffScheduleSwapDTO;
import com.his.outpatient.dto.StaffScheduleUpsertDTO;
import com.his.outpatient.dto.StaffScheduleRejectDTO;
import com.his.outpatient.dto.StaffScheduleWeekActionDTO;
import com.his.outpatient.dto.StaffSchedulePendingQueryDTO;
import com.his.outpatient.dto.StaffScheduleWorkloadDTO;
import com.his.outpatient.entity.BizSchedule;
import com.his.outpatient.vo.StaffCheckVO;
import com.his.outpatient.vo.StaffScheduleVO;
import com.his.outpatient.vo.ErFixedRatioVO;
import com.his.outpatient.vo.StaffTypeDayWorkingVO;
import com.his.outpatient.vo.StaffWorkloadVO;
import com.his.outpatient.vo.UnitDayWorkingVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 全院岗位排班服务 —— 排班事实的唯一写入口。
 */
public interface StaffScheduleService extends IService<BizSchedule> {

    /**
     * 排班事实分页：按日期区间、排班单元、岗位类别、出勤状态与姓名/工号关键词筛出排班行。
     * 用于「全院岗位排班」页的查询卡与表格，也是该行改删与变更留痕的取数入口。
     */
    PageResult<StaffScheduleVO> pageVO(StaffSchedulePageQueryDTO dto);

    /**
     * 新增/修改一条排班。返回带出的提示文案（如超出人力上限），null=无提示。
     */
    String upsert(StaffScheduleUpsertDTO dto);

    /**
     * 出诊计划层用：按「人 × 日 × 班」取得（没有就落一条）对应的排班事实，并复核它挂得出号。
     *
     * <p>与 {@link #upsert} 的区别是这里<b>撞已存在的行不报错而是复用</b> ——
     * 一条出勤事实可以挂多条出诊计划（同一人同一班次在两个科室各开一个号源池），
     * 但反过来不行：号源层不允许自己决定「这个人今天上不上班」。
     * <br>入参带 id 时改的就是那条事实本身（出诊计划换了科室/时段，事实要跟着改）。
     *
     * @param source 生成来源（门诊排班落 1-手工，周模板生成落 2-模板）
     */
    BizSchedule ensureForClinic(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source);

    /**
     * 各排班线共用的落事实入口：按「人 × 日 × 班」取到那条事实（没有就落一条），<b>不看号源</b>。
     *
     * <p>出诊、病区护理、全院值守三条线的差别只在「这条班要不要放号」，
     * 「这个人哪天几点在哪个单元上不上班」是同一件事，所以事实的写法只留这一份。
     */
    BizSchedule ensureAttendance(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source);

    /**
     * 某一个<b>排班单元内</b>的落事实入口：查重键比 {@link #ensureAttendance} 多带排班单元。
     *
     * <p>为什么要有两个口径：
     * <ul>
     *   <li><b>门诊</b>（{@link #ensureForClinic} → {@link #ensureAttendance}）刻意用「人 × 日 × 班」这个
     *       <b>不含单元</b>的键 —— 一条出勤事实可以挂多个科室的号源池（同一人同一班次在两个科室各开一池号）。</li>
     *   <li><b>病区护理 / 值守</b>必须用含单元的键：这两个格子的身份是「人 × 日 × 单元」，
     *       同一个人同一天在不同单元各有一格是完全正常的。用不含单元的键会在
     *       「他今天在门诊已有一条事实」时复用那条，写出来的结果是
     *       <b>护理表说她在病区、底座说她在门诊</b>（G-10 错位）。</li>
     * </ul>
     *
     * @param source 生成来源
     * @return 那条事实（新增或被复用的那条），调用方要拿它的 id 回写自己的回指列
     */
    BizSchedule ensureForUnit(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source);

    /**
     * 按「人 × 日」覆盖一条事实：先清掉该单元当天这个人的旧行，再落新行。
     *
     * <p>护理与值守那条线的格子身份是<b>一天一格</b>（唯一键不含班次），
     * 把白班改成夜班在格子上是「改」，在事实层却是一个新键 —— 不先让位就会留下两条同日事实，
     * 而事实层的键谁也覆盖不了谁。
     */
    BizSchedule replaceDayAttendance(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source);

    /**
     * 清掉某个单元里这个人当天的事实行（物理删）。格子删掉 = 这个人这天在这个单元没班了。
     */
    void purgeDayAttendance(Integer orgType, Long orgId, Long employeeId, LocalDate date);

    /**
     * 删除排班（物理删，唯一键不含删除标志）。
     */
    void deleteById(Long id);

    /**
     * 换班（两条排班对调排班对象）或代班（单向换人）。双方都按新班次重校验时间重叠。
     */
    void swap(StaffScheduleSwapDTO dto);

    /**
     * 按星期对齐整周复制排班。
     *
     * @return 复制出来的行数
     */
    int copyRange(StaffScheduleCopyDTO dto);

    /**
     * 此刻在岗名单。跨零点班归开始日，所以会同时捞「今天」与「昨天」两天的行。
     *
     * @param at        判定时刻
     * @param orgType   单元类型（空=全部）
     * @param orgId     单元ID（空=该类型全部单元）
     * @param staffType 岗位类别（空=全部岗位）
     */
    List<StaffScheduleVO> onDutyAt(LocalDateTime at, Integer orgType, Long orgId, Integer staffType);

    /**
     * 急诊值班公开视图（满足核心制度「值班表全院公开」）：按日期返回急诊内科/外科/儿科
     * 三个科室已发布（publish_status ∈ 2,4）的医生出勤行，按班次开始时间排序。
     * 只读、无数据范围限制 —— 全院登录职工均可查看当天的急诊值班表。
     */
    List<StaffScheduleVO> erDutyPublic(LocalDate date);

    /**
     * 急诊固定医师占比（评审口径：固定急诊医师 ≥ 在岗急诊医师 75%）。
     * 按急诊内科/外科/儿科分别统计：当日已发布（2/4）在岗医生中，档案标记固定（is_fixed=1）的占比。
     */
    List<ErFixedRatioVO> erFixedRatio(LocalDate date);

    /**
     * 某天的排班行（下游按实体取号源派生所需的字段，不重复查一次班次）。
     */
    List<BizSchedule> listDay(LocalDate date, Integer orgType, Long orgId, Integer staffType);

    /**
     * 这条排班是否该产出出诊计划与号源（岗位有号源属性 + 出勤 + 非听班 + 标记出诊）。
     */
    boolean releasesClinicSource(BizSchedule schedule);

    /**
     * 排班单元 × 日期的在岗人次（总览矩阵格子）。
     */
    List<UnitDayWorkingVO> listUnitDayWorking(LocalDate begin, LocalDate end);

    /**
     * 岗位类别 × 日期的在岗人次（总览卡片）。
     */
    List<StaffTypeDayWorkingVO> listStaffTypeDayWorking(LocalDate begin, LocalDate end);

    /**
     * 区间规则校验（全区岗位通用）：连续夜班 / 连续上班 / 单周工时 / 班次冲突 / 人力缺口与富余。
     * 只列告警明细不阻断保存，排班员自查用。
     */
    StaffCheckVO check(StaffScheduleCheckDTO dto);

    /**
     * 月度工时统计（全区岗位通用）：一人一行，含整段未排班的人（科室/病区口径补齐）。
     */
    StaffWorkloadVO monthWorkload(StaffScheduleWorkloadDTO dto);

    // ===== 发布态流转：提交 → 审核发布 / 驳回 / 撤回 / 调班 =====
    // 审核动作必须命中科室主档指定的全院岗位排班审核人（系统管理员豁免；orgType=3 全院级仅管理员）。

    /**
     * 提交审核：草稿(0) → 待审核(1)。任何有排班录入权限的人都能提交。
     */
    void submit(Long id);

    /**
     * 审核发布：待审核(1) → 已发布(2)。当前登录人须为本科室指定审核人，否则驳回。
     */
    void auditPublish(Long id);

    /**
     * 驳回：待审核(1) → 草稿(0)，并写入驳回原因留痕。
     */
    void reject(StaffScheduleRejectDTO dto);

    /**
     * 撤回：任意态 → 草稿(0)（撤回后不计在岗）。
     */
    void recall(Long id);

    /**
     * 进入调班：已发布/已变更(2/4) → 调班中(3)。
     */
    void adjustStart(Long id);

    /**
     * 整周提交：本单元整周（周一～周日）草稿 → 待审核。
     *
     * @return 实际迁移行数（为 0 抛异常，提示本周没有待提交的草稿）
     */
    int submitWeek(StaffScheduleWeekActionDTO dto);

    /**
     * 整周审核发布：本单元整周（周一～周日）待审核 → 已发布，需命中科室指定审核人。
     *
     * @return 实际迁移行数（为 0 抛异常，提示本周没有待审核的排班）
     */
    int auditWeek(StaffScheduleWeekActionDTO dto);

    /**
     * 待我审批（聚合）：返回当前登录人作为指定审核人的待审核排班（全院岗位 + 护理）。
     * 系统管理员看全院的待审；普通审核人只看本科室主档指定的那几个科室/病区。
     */
    PageResult<StaffScheduleVO> pendingApprovalPage(StaffSchedulePendingQueryDTO dto);

    /**
     * 排班单元候选（页面按单元过滤、整周提交审核时选单元用）。
     *
     * @param orgType 单元类型（1-科室 2-病区 3-全院）；为空则三类全给
     */
    List<StaffScheduleVO.UnitOption> unitSelectList(Integer orgType);
}
