package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizShift;
import com.his.system.entity.BizStaffAttendance;
import com.his.system.entity.BizSchedule;
import com.his.system.vo.CalibrationAdviceVO;
import com.his.system.vo.StaffWorktimeVO;
import com.his.system.vo.WorktimeSummaryVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 实际出勤 Mapper。
 */
@Mapper
public interface BizStaffAttendanceMapper extends BaseMapper<BizStaffAttendance> {

    /**
     * 按唯一键取一条出勤（人 × 日 × 单元 × 班次）。
     * <br>命中即幂等：重复签到是同一次出勤的再次刷卡，不该生成第二条记录。
     */
    @Select("SELECT * FROM biz_staff_attendance WHERE del_flag = 0 "
            + "AND employee_id = #{employeeId} AND schedule_date = #{date} "
            + "AND org_type = #{orgType} AND org_id = #{orgId} AND shift_id = #{shiftId} LIMIT 1")
    BizStaffAttendance selectOneAttend(@Param("employeeId") Long employeeId,
                                       @Param("date") LocalDate date,
                                       @Param("orgType") Integer orgType,
                                       @Param("orgId") Long orgId,
                                       @Param("shiftId") Long shiftId);

    /**
     * 物理删单行。
     */
    @Delete("DELETE FROM biz_staff_attendance WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    /**
     * 这个人当天的全部出勤（一个单元一行，跨两个单元支援就是两行）
     */
    @Select("SELECT * FROM biz_staff_attendance WHERE del_flag = 0 "
            + "AND employee_id = #{employeeId} AND schedule_date = #{date}")
    List<BizStaffAttendance> selectDayOfEmployee(@Param("employeeId") Long employeeId,
                                                 @Param("date") LocalDate date);

    /**
     * 这个人当天"应上班"的计划行（只读对照用）。
     */
    @Select("SELECT * FROM biz_schedule WHERE del_flag = 0 "
            + "AND employee_id = #{employeeId} AND schedule_date = #{date} AND duty_status = 1")
    List<BizSchedule> selectDayPlanOfEmployee(@Param("employeeId") Long employeeId,
                                                   @Param("date") LocalDate date);

    /**
     * 按 id 取一条计划事实（签退时要把这条记录放回它自己的班，才知道几点该下班）
     */
    @Select("SELECT * FROM biz_schedule WHERE del_flag = 0 AND id = #{id}")
    BizSchedule selectPlanById(@Param("id") Long id);

    /**
     * 迟到宽限（分钟）—— 判定参数存在班次字典里，不写死在代码里
     */
    @Select("SELECT late_grace_minutes FROM biz_shift WHERE id = #{shiftId} AND del_flag = 0")
    Integer selectLateGrace(@Param("shiftId") Long shiftId);

    /**
     * 班次快照（含起止时间与迟到宽限）
     */
    @Select("SELECT id, shift_name, start_time, end_time, cross_day, is_night, duration_minutes, late_grace_minutes "
            + "FROM biz_shift WHERE id = #{shiftId} AND del_flag = 0")
    BizShift selectShift(@Param("shiftId") Long shiftId);


    /**
     * 行级对照（计划 vs 实际）。聚合逻辑见 mapper/BizStaffAttendanceMapper.xml 的 worktimeBase，
     * 原库视图 v_staff_worktime 已退役（视图依赖旧表 biz_staff_schedule，表改名后失效）。
     */
    List<StaffWorktimeVO> selectComparison(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("orgType") Integer orgType,
                                           @Param("orgId") Long orgId,
                                           @Param("staffType") Integer staffType,
                                           @Param("employeeId") Long employeeId,
                                           @Param("diffType") Integer diffType);

    /**
     * 单元 × 日 执行汇总。聚合逻辑见 mapper/BizStaffAttendanceMapper.xml 的 summaryAgg，
     * 原库视图 v_staff_worktime_summary 已退役。
     */
    List<WorktimeSummaryVO> selectSummary(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("orgType") Integer orgType,
                                          @Param("orgId") Long orgId,
                                          @Param("staffType") Integer staffType);

    /**
     * 编制校准建议（近 14 天）。聚合逻辑见 mapper/BizStaffAttendanceMapper.xml 的 adviceAgg，
     * 原库视图 v_staff_calibration_advice 已退役。
     */
    List<CalibrationAdviceVO> selectAdvice(@Param("orgType") Integer orgType,
                                           @Param("orgId") Long orgId,
                                           @Param("staffType") Integer staffType);
}
