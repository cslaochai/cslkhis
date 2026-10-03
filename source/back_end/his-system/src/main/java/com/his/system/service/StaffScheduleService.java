package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.common.enums.StaffScheduleSourceEnum;
import com.his.system.dto.StaffScheduleCopyDTO;
import com.his.system.dto.StaffScheduleQueryPageDTO;
import com.his.system.dto.StaffScheduleSwapDTO;
import com.his.system.dto.StaffScheduleUpsertDTO;
import com.his.system.entity.BizStaffSchedule;
import com.his.system.vo.StaffOnDutyVO;
import com.his.system.vo.StaffScheduleVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 全院岗位排班服务 —— 排班事实的唯一写入口。
 *
 * <p>出诊计划与号源、值班责任位都是它的<b>下游</b>：下游不得自己造「人与时间」，
 * 只能由这里的行派生。这样「同一个人被排到两个重叠时段」才有一个地方能拦住。
 */
public interface StaffScheduleService extends IService<BizStaffSchedule> {

    PageResult<StaffScheduleVO> pageVO(StaffScheduleQueryPageDTO dto);

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
    BizStaffSchedule ensureForClinic(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source);

    /**
     * 各排班线共用的落事实入口：按「人 × 日 × 班」取到那条事实（没有就落一条），<b>不看号源</b>。
     *
     * <p>出诊、病区护理、全院值守三条线的差别只在「这条班要不要放号」，
     * 「这个人哪天几点在哪个单元上不上班」是同一件事，所以事实的写法只留这一份。
     */
    BizStaffSchedule ensureAttendance(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source);

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
    BizStaffSchedule ensureForUnit(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source);

    /**
     * 按「人 × 日」覆盖一条事实：先清掉该单元当天这个人的旧行，再落新行。
     *
     * <p>护理与值守那条线的格子身份是<b>一天一格</b>（唯一键不含班次），
     * 把白班改成夜班在格子上是「改」，在事实层却是一个新键 —— 不先让位就会留下两条同日事实，
     * 而事实层的键谁也覆盖不了谁。
     */
    BizStaffSchedule replaceDayAttendance(StaffScheduleUpsertDTO dto, StaffScheduleSourceEnum source);

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
    List<StaffOnDutyVO> onDutyAt(LocalDateTime at, Integer orgType, Long orgId, Integer staffType);

    /**
     * 某天的排班行（下游按实体取号源派生所需的字段，不重复查一次班次）。
     */
    List<BizStaffSchedule> listDay(LocalDate date, Integer orgType, Long orgId, Integer staffType);

    /**
     * 这条排班是否该产出出诊计划与号源（岗位有号源属性 + 出勤 + 非听班 + 标记出诊）。
     */
    boolean releasesClinicSource(BizStaffSchedule schedule);
}
