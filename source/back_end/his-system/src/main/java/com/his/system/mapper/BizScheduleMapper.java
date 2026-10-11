package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizSchedule;
import com.his.system.vo.StaffWorkingGroupVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 全院岗位排班 Mapper。
 */
@Mapper
public interface BizScheduleMapper extends BaseMapper<BizSchedule> {

    /**
     * 物理删单行。
     */
    @Delete("DELETE FROM biz_schedule WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    /**
     * 物理删掉某个单元里这个人当天的全部事实行（改格/删格用，同样撞键所以不能软删）。
     */
    @Delete("DELETE FROM biz_schedule WHERE org_type = #{orgType} AND org_id = #{orgId} "
            + "AND employee_id = #{employeeId} AND schedule_date = #{scheduleDate} "
            + "AND (#{keepId} IS NULL OR id <> #{keepId})")
    int purgeByDay(@Param("orgType") Integer orgType, @Param("orgId") Long orgId,
                   @Param("employeeId") Long employeeId, @Param("scheduleDate") LocalDate scheduleDate,
                   @Param("keepId") Long keepId);

    /**
     * 在岗人次聚合（日期 × 单元 × 班次 × 岗位）：总览矩阵与人力缺口对比共用一次扫描。
     */
    @Select("SELECT schedule_date AS scheduleDate, org_type AS orgType, org_id AS orgId, "
            + "MAX(org_name) AS orgName, shift_id AS shiftId, staff_type AS staffType, COUNT(*) AS cnt "
            + "FROM biz_schedule "
            + "WHERE del_flag = 0 AND duty_status = 1 AND schedule_date BETWEEN #{begin} AND #{end} "
            + "GROUP BY schedule_date, org_type, org_id, shift_id, staff_type")
    List<StaffWorkingGroupVO> groupWorkingByUnitShift(@Param("begin") LocalDate begin,
                                                      @Param("end") LocalDate end);
}

