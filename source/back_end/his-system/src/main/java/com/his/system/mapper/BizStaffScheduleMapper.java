package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizStaffSchedule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 全院岗位排班 Mapper。
 */
@Mapper
public interface BizStaffScheduleMapper extends BaseMapper<BizStaffSchedule> {

    /**
     * 物理删单行。
     * <br>本表唯一键「人 × 日期 × 班次」不含删除标志，软删留下的行继续占键，
     * 于是「删掉再重排同一天同一班」必然撞重复键 —— 配置类排班行没有留档价值，直接物理删。
     * 换班/停班的历史由变更留痕承担，不靠这里的软删行。
     */
    @Delete("DELETE FROM biz_staff_schedule WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    /**
     * 物理删掉某个单元里这个人当天的全部事实行（改格/删格用，同样撞键所以不能软删）。
     * <br>按单元收口而不是按人按日全删：同一个人可以在病区上一格、又在门诊出诊，
     * 两条线各写各的行，改护理格子不能把门诊那条一起洗掉。
     */
    @Delete("DELETE FROM biz_staff_schedule WHERE org_type = #{orgType} AND org_id = #{orgId} "
            + "AND employee_id = #{employeeId} AND schedule_date = #{scheduleDate} "
            + "AND (#{keepId} IS NULL OR id <> #{keepId})")
    int purgeByDay(@Param("orgType") Integer orgType, @Param("orgId") Long orgId,
                   @Param("employeeId") Long employeeId, @Param("scheduleDate") LocalDate scheduleDate,
                   @Param("keepId") Long keepId);

    /**
     * 在岗人次聚合（日期 × 单元 × 班次 × 岗位）：总览矩阵与人力缺口对比共用一次扫描。
     * org_name 用 MAX 取快照（0 人上班的单元不会出现在结果里，名称缺口由标准行补）。
     */
    @Select("SELECT DATE_FORMAT(schedule_date, '%Y-%m-%d') AS scheduleDate, org_type AS orgType, org_id AS orgId, "
            + "MAX(org_name) AS orgName, shift_id AS shiftId, staff_type AS staffType, COUNT(*) AS cnt "
            + "FROM biz_staff_schedule "
            + "WHERE del_flag = 0 AND duty_status = 1 AND schedule_date BETWEEN #{begin} AND #{end} "
            + "GROUP BY schedule_date, org_type, org_id, shift_id, staff_type")
    List<Map<String, Object>> groupWorkingByUnitShift(@Param("begin") LocalDate begin,
                                                      @Param("end") LocalDate end);
}

