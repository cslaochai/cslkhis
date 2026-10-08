package com.his.medicaltech.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 检查申请单的预约侧写入口。
 */
@Mapper
public interface ExamApplyWriterMapper {

    /**
     * 预约成功：状态推到 3-已预约，并把预约时刻写进 appointment_time
     */
    @Update("UPDATE biz_inspection_apply SET apply_status = 3, appointment_time = #{appointmentTime}, "
            + "update_by = #{operator}, update_time = NOW() WHERE id = #{applyId} AND del_flag = 0")
    int markBooked(@Param("applyId") Long applyId, @Param("appointmentTime") LocalDateTime appointmentTime,
                   @Param("operator") String operator);

    /**
     * 到检：申请单进入 4-检查中（只在确实停在 3 时推进，不覆盖别人的状态）
     */
    @Update("UPDATE biz_inspection_apply SET apply_status = 4, update_by = #{operator}, update_time = NOW() "
            + "WHERE id = #{applyId} AND del_flag = 0 AND apply_status = 3")
    int markArrived(@Param("applyId") Long applyId, @Param("operator") String operator);

    /**
     * 取消/爽约：按预约前的状态精确回退，并清空预约时刻
     */
    @Update("UPDATE biz_inspection_apply SET apply_status = #{prevStatus}, appointment_time = NULL, "
            + "update_by = #{operator}, update_time = NOW() WHERE id = #{applyId} AND del_flag = 0 AND apply_status = 3")
    int revertBooked(@Param("applyId") Long applyId, @Param("prevStatus") Integer prevStatus,
                     @Param("operator") String operator);
}
