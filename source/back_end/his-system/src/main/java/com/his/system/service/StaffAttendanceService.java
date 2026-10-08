package com.his.system.service;

import com.his.system.dto.AttendanceDTO;
import com.his.system.vo.CalibrationAdviceVO;
import com.his.system.vo.StaffAttendanceVO;
import com.his.system.vo.StaffWorktimeVO;
import com.his.system.vo.WorktimeSummaryVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 实际出勤服务（闭环第 3 步：执行与回填）。
 */
public interface StaffAttendanceService {

    /**
     * 签到。
     * <p>幂等：同一个人同一天同一单元同一班次重复刷卡不产生第二条记录（唯一键收口），
     * 且<b>保留最早那次签到时间</b> —— 刷卡两次不该把迟到刷成准时。
     * <p>没排班也能签（记为加班）；排了病区却出现在别的单元（记为支援）。
     */
    StaffAttendanceVO checkIn(AttendanceDTO dto);

    /**
     * 签退：算出实际工时、超时工时，并给出迟到/早退判定。
     * <p>迟到/早退按「班次日期 + 班次起止」构造完整时刻来比，
     * 跨零点的班（22:00→次日08:00）把结束时间顺延一天 —— 只比时刻会漏判。
     */
    StaffAttendanceVO checkOut(AttendanceDTO dto);

    /**
     * 确认缺勤 —— <b>这是全系统唯一能产生"缺勤"的入口</b>。
     * <p>前提是该员工当天真有"上班"排班：没排班就谈不上缺勤。
     * 已经有签到记录的不给确认（先撤销出勤登记再说）。
     */
    StaffAttendanceVO markAbsent(AttendanceDTO dto);

    /**
     * 手工登记/修正工时（给没打卡的日子补账，由护士长操作）。
     * <p>写了 {@code confirmStatus = 1}：这一步本身就是人在确认，不必再确认一遍。
     */
    StaffAttendanceVO adjust(AttendanceDTO dto);

    /**
     * 科室确认（0-待确认 1-已确认 2-有异议）。
     * <p>有异议的行会留在榜单上等复核，不会被静默抹平。
     */
    void confirm(Long id, Integer confirmStatus);

    /**
     * 撤销一条出勤登记（物理删）。
     * <p>为什么是物理删：唯一键不含删除标志，软删行继续占键，
     * 撤销后重新签到必然撞重复键。出勤登记本身不是留档对象，错了就该彻底消失。
     */
    void remove(Long id);

    /** 计划 vs 实际 的行级对照（可按需筛到某个单元/某个人/某类差异）。 */
    List<StaffWorktimeVO> comparison(LocalDate startDate, LocalDate endDate, Integer orgType, Long orgId,
                                     Integer staffType, Integer diffType);

    /** 单元 × 日 的执行汇总（计划人数/实到/缺勤/未回填/加班 + 工时差）。 */
    List<WorktimeSummaryVO> summary(LocalDate startDate, LocalDate endDate, Integer orgType, Long orgId,
                                    Integer staffType);

    /**
     * 差异归因 → 编制校准建议（闭环第 4 步）。
     * <p>只给建议，不自动改编制：编制是护理部的权，系统给出数、人做决定。
     */
    List<CalibrationAdviceVO> advice(Integer orgType, Long orgId, Integer staffType);
}
