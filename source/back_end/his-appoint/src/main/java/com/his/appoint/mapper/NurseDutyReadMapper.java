package com.his.appoint.mapper;

import com.his.appoint.dto.NurseDutyRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 跨模块只读护理排班（biz_schedule 护理序列行）。
 *
 * <p>护理排班已并入排班底座（2026-10-11）：在岗护士 = {@code biz_schedule} 里
 * {@code staff_type IN (2,7)}（护理/护工护理辅助）的行，护理不再有独立副本表。
 * 此处以裸 SQL 直接读 his-system 的表，不引入模块编译依赖。
 */
@Mapper
public interface NurseDutyReadMapper {

    /**
     * 取某科室某天在岗（上班）护士。
     *
     * @param deptId 科室 ID（门诊科室或病区所属科室）
     * @param date   排班日期
     */
    @Select("SELECT id, employee_id, employee_name AS nurse_name, dept_id, dept_name, "
            + "NULLIF(shift_id, 0) AS shift_id, start_time, end_time, duty_status AS schedule_status "
            + "FROM biz_schedule "
            + "WHERE del_flag = 0 AND staff_type IN (2, 7) "
            + "AND dept_id = #{deptId} AND schedule_date = #{date} AND duty_status = 1")
    List<NurseDutyRow> listOnDuty(@Param("deptId") Long deptId, @Param("date") LocalDate date);
}
