package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizExamAppointment;
import com.his.medicaltech.vo.ExamApptStatusCountRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BizExamAppointmentMapper extends BaseMapper<BizExamAppointment> {

    /**
     * 终结预约单（active_flag 置 NULL，释放「同一申请单只能有一条在办预约」的唯一索引位）。
     *
     * <p>必须走本方法：MyBatis-Plus 的 updateById 默认不更新 null 字段（NOT_NULL 策略），
     * 把 activeFlag 设为 null 再 updateById 会静默跳过该列，结果是"单子看起来取消了、
     * 唯一索引位还占着"，同一张申请单再也约不上。
     */
    @Update("UPDATE biz_exam_appointment SET active_flag = NULL, update_time = NOW() "
            + "WHERE id = #{id} AND del_flag = 0")
    int markInactive(@Param("id") Long id);

    /**
     * 行锁读单条预约单（改约/取消/到检按同一顺序先锁自己）
     */
    @Select("SELECT * FROM biz_exam_appointment WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    BizExamAppointment selectForUpdate(@Param("id") Long id);

    /**
     * 锁定「同一患者同一检查日」的全部在办预约，用于患者跨设备撞车检测。
     *
     * <p>命中索引 idx_exam_appt_patient_date；结果为空时 InnoDB 仍会加间隙锁，
     * 因此同一患者同一天的两笔并发预约会互相等待而不是双双通过检查后各占一台机器。
     */
    @Select("SELECT * FROM biz_exam_appointment WHERE patient_id = #{patientId} AND exam_date = #{examDate} "
            + "AND active_flag = 1 AND status IN (1, 2) AND del_flag = 0 ORDER BY start_time ASC FOR UPDATE")
    List<BizExamAppointment> selectPatientDayForUpdate(@Param("patientId") Long patientId,
                                                       @Param("examDate") LocalDate examDate);

    /**
     * 某设备某日的在办/已完成占号（status 1/2/3 —— 取消与爽约不占格子）
     */
    @Select("SELECT * FROM biz_exam_appointment WHERE device_id = #{deviceId} AND exam_date = #{slotDate} "
            + "AND status IN (1, 2, 3) AND del_flag = 0 ORDER BY start_time ASC")
    List<BizExamAppointment> selectOccupants(@Param("deviceId") Long deviceId,
                                             @Param("slotDate") LocalDate slotDate);

    /**
     * 号源对账用：按设备+日期区间取占号事实（deviceId 传 null = 全院；一次取回内存算，不逐格 count）
     */
    @Select("SELECT * FROM biz_exam_appointment WHERE exam_date BETWEEN #{dateFrom} AND #{dateTo} "
            + "AND status IN (1, 2, 3) AND del_flag = 0 "
            + "AND (#{deviceId, jdbcType=BIGINT} IS NULL OR device_id = #{deviceId, jdbcType=BIGINT})")
    List<BizExamAppointment> selectRangeOccupants(@Param("deviceId") Long deviceId,
                                                  @Param("dateFrom") LocalDate dateFrom,
                                                  @Param("dateTo") LocalDate dateTo);

    /**
     * 到检超时扫描：停在「已预约」且时段已过的单
     */
    @Select("SELECT * FROM biz_exam_appointment WHERE status = 1 AND active_flag = 1 AND del_flag = 0 "
            + "AND (exam_date < CURDATE() OR (exam_date = CURDATE() AND end_time <= DATE_FORMAT(NOW(), '%H:%i')))")
    List<BizExamAppointment> selectOverdueNoShow();

    /**
     * 按状态分组统计（deviceId/slotDate 传 null 表示该维度不限；前端不得拿当前页 list 自己数）
     */
    @Select("SELECT status, COUNT(*) AS cnt FROM biz_exam_appointment WHERE del_flag = 0 "
            + "AND (#{deviceId, jdbcType=BIGINT} IS NULL OR device_id = #{deviceId, jdbcType=BIGINT}) "
            + "AND (#{slotDate, jdbcType=DATE} IS NULL OR exam_date = #{slotDate, jdbcType=DATE}) "
            + "GROUP BY status")
    List<ExamApptStatusCountRowVO> countGroupByStatus(@Param("deviceId") Long deviceId,
                                                 @Param("slotDate") LocalDate slotDate);
}
