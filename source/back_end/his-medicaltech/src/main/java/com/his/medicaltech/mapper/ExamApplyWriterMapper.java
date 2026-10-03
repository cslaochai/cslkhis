package com.his.medicaltech.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 检查申请单的预约侧写入口。
 *
 * <p>为什么自带一个 mapper 而不复用 his-emr 的 {@code BizInspectionApplyMapper.updateById}：
 * 取消预约要把 {@code appointment_time} 置回 NULL，而 MyBatis-Plus 的 updateById
 * 在 NOT_NULL 策略下会静默跳过 null 字段 —— 现象是"预约取消了，申请单上还挂着预约时间"，
 * 且不报错。写 NULL 必须显式 SQL。
 *
 * <p>表属 his-emr，但这里是"预约动作对申请单状态的影响"，与本模块的预约事实同源，
 * 因此按本模块的先例（{@code MedicalTechServiceImpl} 直接引用申请单）在本模块落地。
 */
@Mapper
public interface ExamApplyWriterMapper {

    /** 预约成功：状态推到 3-已预约，并把预约时刻写进 appointment_time */
    @Update("UPDATE biz_inspection_apply SET apply_status = 3, appointment_time = #{appointmentTime}, "
            + "update_by = #{operator}, update_time = NOW() WHERE id = #{applyId} AND del_flag = 0")
    int markBooked(@Param("applyId") Long applyId, @Param("appointmentTime") LocalDateTime appointmentTime,
                   @Param("operator") String operator);

    /** 到检：申请单进入 4-检查中（只在确实停在 3 时推进，不覆盖别人的状态） */
    @Update("UPDATE biz_inspection_apply SET apply_status = 4, update_by = #{operator}, update_time = NOW() "
            + "WHERE id = #{applyId} AND del_flag = 0 AND apply_status = 3")
    int markArrived(@Param("applyId") Long applyId, @Param("operator") String operator);

    /** 取消/爽约：按预约前的状态精确回退，并清空预约时刻 */
    @Update("UPDATE biz_inspection_apply SET apply_status = #{prevStatus}, appointment_time = NULL, "
            + "update_by = #{operator}, update_time = NOW() WHERE id = #{applyId} AND del_flag = 0 AND apply_status = 3")
    int revertBooked(@Param("applyId") Long applyId, @Param("prevStatus") Integer prevStatus,
                     @Param("operator") String operator);
}
