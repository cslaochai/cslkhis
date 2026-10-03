package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizStaffSchedule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

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
}

