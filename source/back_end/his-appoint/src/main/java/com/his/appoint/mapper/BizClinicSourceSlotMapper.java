package com.his.appoint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.appoint.entity.BizClinicSourceSlot;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 排班时间片段Mapper。
 */
@Mapper
public interface BizClinicSourceSlotMapper extends BaseMapper<BizClinicSourceSlot> {

    /**
     * 现场渠道原子扣段（窗口1/自助2/网上3）：WHERE available_source > 0 兜住并发抢号；
     * 段内已划预约池（appointment_source > 0）的，现场可占号 = 段剩余 - 段池剩余，
     * 即现场不许吃掉段内预约池剩余。返回 0 表示该段现场可占号已空。
     * status = 1 条件：停诊/停用段不可再挂。
     */
    @Update("UPDATE biz_clinic_source_slot SET used_source = used_source + 1, available_source = available_source - 1 "
            + "WHERE id = #{slotId} AND status = 1 AND available_source > 0 "
            + "AND (appointment_source = 0 OR available_source - (appointment_source - used_appointment_source) > 0)")
    int deductSlotSourceForWalkin(@Param("slotId") Long slotId);

    /**
     * 预约渠道原子扣段（regist_source=4）：必须从段内预约池扣，
     * 同时扣段总池（available_source）与段池（used_appointment_source）。
     * 返回 0 表示该段预约池剩余已空。
     */
    @Update("UPDATE biz_clinic_source_slot SET used_source = used_source + 1, available_source = available_source - 1, "
            + "used_appointment_source = used_appointment_source + 1 "
            + "WHERE id = #{slotId} AND status = 1 AND available_source > 0 "
            + "AND appointment_source - used_appointment_source > 0")
    int deductSlotSourceForAppointment(@Param("slotId") Long slotId);

    /**
     * 现场渠道释放段（退号/换号）：WHERE used_source > 0 防止重复释放减成负数。
     * 不带 status 条件：停诊退号也要能还号。
     */
    @Update("UPDATE biz_clinic_source_slot SET used_source = used_source - 1, available_source = available_source + 1 "
            + "WHERE id = #{slotId} AND used_source > 0")
    int releaseSlotSourceForWalkin(@Param("slotId") Long slotId);

    /**
     * 预约渠道释放段：段池已用 GREATEST 兜底不为负
     * （池计数被修正等场景下，段总号仍正确释放）。
     */
    @Update("UPDATE biz_clinic_source_slot SET used_source = used_source - 1, available_source = available_source + 1, "
            + "used_appointment_source = GREATEST(used_appointment_source - 1, 0) "
            + "WHERE id = #{slotId} AND used_source > 0 AND used_appointment_source > 0")
    int releaseSlotSourceForAppointment(@Param("slotId") Long slotId);

    /**
     * 物理删除排班下的全部段。uk_slot(schedule_id, start_time) 不含 del_flag（铁律），
     * 逻辑删的行会继续占用唯一键 → 排班删除/时间窗重建必须物理删。
     */
    @Delete("DELETE FROM biz_clinic_source_slot WHERE schedule_id = #{scheduleId}")
    int physicalDeleteByScheduleId(@Param("scheduleId") Long scheduleId);
}
