package com.his.appoint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.appoint.entity.BizSchedule;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 排班信息Mapper
 */
@Mapper
public interface BizScheduleMapper extends BaseMapper<BizSchedule> {

    /**
     * 物理删除排班。uk_schedule_window(dept_id, doctor_id, schedule_date, start_time, end_time)
     * 不含 del_flag（铁律：唯一索引不含 del_flag），BaseEntity 的 @TableLogic 逻辑删
     * 会留下继续占用唯一键的行 → 同窗口新排班永远插不进去，删除必须物理删。
     */
    @Delete("DELETE FROM biz_schedule WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    /**
     * 现场渠道原子扣减号源（窗口1/自助2/网上3）：WHERE available_source > 0 兜住并发抢号；
     * 已划预约池（appointment_source > 0）的班次，现场可占号 = 剩余号源 - 预约池剩余，
     * 即现场不许吃掉预约池剩余（appointment_source - used_appointment_source）。
     * 返回 0 表示现场可占号已空（号满或只剩预约池）。
     */
    @Update("UPDATE biz_schedule SET used_source = used_source + 1, available_source = available_source - 1 "
            + "WHERE id = #{scheduleId} AND available_source > 0 "
            + "AND (appointment_source IS NULL OR appointment_source = 0 "
            + "     OR available_source - (appointment_source - used_appointment_source) > 0)")
    int deductScheduleSourceForWalkin(@Param("scheduleId") Long scheduleId);

    /**
     * 预约渠道原子扣减号源（regist_source=4）：必须从预约池内扣，
     * 同时扣总池（available_source）与预约池（used_appointment_source）。
     * 返回 0 表示预约池剩余已空。
     */
    @Update("UPDATE biz_schedule SET used_source = used_source + 1, available_source = available_source - 1, "
            + "used_appointment_source = used_appointment_source + 1 "
            + "WHERE id = #{scheduleId} AND available_source > 0 "
            + "AND appointment_source - used_appointment_source > 0")
    int deductScheduleSourceForAppointment(@Param("scheduleId") Long scheduleId);

    /**
     * 现场渠道释放号源（退号/换号）：WHERE used_source > 0 防止重复释放减成负数
     */
    @Update("UPDATE biz_schedule SET used_source = used_source - 1, available_source = available_source + 1 "
            + "WHERE id = #{scheduleId} AND used_source > 0")
    int releaseScheduleSourceForWalkin(@Param("scheduleId") Long scheduleId);

    /**
     * 预约渠道释放号源（退号）：总池照常还号，预约池已用 GREATEST 兜底不为负
     * （池被清空等数据修正场景下，总号仍正确释放）。
     */
    @Update("UPDATE biz_schedule SET used_source = used_source - 1, available_source = available_source + 1, "
            + "used_appointment_source = GREATEST(used_appointment_source - 1, 0) "
            + "WHERE id = #{scheduleId} AND used_source > 0 AND used_appointment_source > 0")
    int releaseScheduleSourceForAppointment(@Param("scheduleId") Long scheduleId);

    /**
     * 门诊号源按日汇总（总览驾驶舱）：班次数 / 总号源 / 已挂 / 停诊班次数。
     */
    @Select("SELECT DATE_FORMAT(schedule_date, '%Y-%m-%d') AS scheduleDate, COUNT(*) AS shiftCount, "
            + "IFNULL(SUM(total_source), 0) AS totalSource, IFNULL(SUM(used_source), 0) AS usedSource, "
            + "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS stoppedCount "
            + "FROM biz_schedule "
            + "WHERE del_flag = 0 AND schedule_date BETWEEN #{begin} AND #{end} "
            + "GROUP BY schedule_date")
    List<Map<String, Object>> summaryByDay(@Param("begin") LocalDate begin, @Param("end") LocalDate end);
}
