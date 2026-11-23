package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizDispenseQueue;
import com.his.pharmacy.vo.DispenseWaitingRxVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 门诊取药队列 Mapper
 */
@Mapper
public interface BizDispenseQueueMapper extends BaseMapper<BizDispenseQueue> {

    /**
     * 取药队列分页（取药日期 + 状态 + 患者姓名/处方号模糊 + 窗口）
     *
     * <p>排序按签到先后：窗口叫号取的就是本页第一条，队列顺序与签到顺序必须一致，
     * 否则「先来的后叫」在前端和叫号屏上都没法解释。
     */
    @Select("<script>" +
            "SELECT * FROM biz_dispense_queue " +
            "WHERE del_flag = 0 " +
            "<if test='queueDate != null'> AND queue_date = #{queueDate} </if> " +
            "<if test='queueStatus != null'> AND queue_status = #{queueStatus} </if> " +
            "<if test='windowId != null'> AND window_id = #{windowId} </if> " +
            "<if test='patientName != null and patientName != \"\"'> AND patient_name LIKE CONCAT('%', #{patientName}, '%') </if> " +
            "<if test='prescriptionNo != null and prescriptionNo != \"\"'> AND prescription_no LIKE CONCAT('%', #{prescriptionNo}, '%') </if> " +
            "ORDER BY sign_in_time ASC, id ASC" +
            "</script>")
    Page<BizDispenseQueue> selectQueuePage(Page<BizDispenseQueue> page,
                                           @Param("queueDate") LocalDate queueDate,
                                           @Param("queueStatus") Integer queueStatus,
                                           @Param("windowId") Long windowId,
                                           @Param("patientName") String patientName,
                                           @Param("prescriptionNo") String prescriptionNo);

    /**
     * 取当天最早签到、尚未分配到窗口的待取药号
     */
    @Select("SELECT * FROM biz_dispense_queue " +
            "WHERE del_flag = 0 AND queue_date = #{queueDate} AND queue_status = 1 " +
            "AND window_id IS NULL " +
            "ORDER BY sign_in_time ASC, id ASC LIMIT 1")
    BizDispenseQueue selectNextWaiting(@Param("queueDate") LocalDate queueDate);

    /**
     * 把号绑定到窗口并计一次叫号
     *
     * <p>{@code window_id IS NULL} 是条件的一部分：两个窗口同时点「下一位」时，
     * 只有一个能改到这一行（返回值 0 的那个必须重取），否则同一个号会在两块屏上同时出现。
     */
    @Update("UPDATE biz_dispense_queue " +
            "SET window_id = #{windowId}, window_name = #{windowName}, " +
            "    call_count = call_count + 1, call_time = #{callTime}, " +
            "    update_by = #{operator}, update_time = #{callTime} " +
            "WHERE id = #{id} AND queue_status = 1 AND window_id IS NULL")
    int bindWindow(@Param("id") Long id,
                   @Param("windowId") Long windowId,
                   @Param("windowName") String windowName,
                   @Param("callTime") LocalDateTime callTime,
                   @Param("operator") String operator);

    /**
     * 该窗口当前正在叫的号（最近一次叫号且仍未发药），叫号屏轮询用
     */
    @Select("SELECT * FROM biz_dispense_queue " +
            "WHERE del_flag = 0 AND window_id = #{windowId} AND queue_status = 1 " +
            "ORDER BY call_time DESC, id DESC LIMIT 1")
    BizDispenseQueue selectCurrentCall(@Param("windowId") Long windowId);

    /**
     * 该窗口最近叫的、允许重叫的号（同一行再叫一次，号不变）
     */
    @Update("UPDATE biz_dispense_queue " +
            "SET call_count = call_count + 1, call_time = #{callTime}, " +
            "    update_by = #{operator}, update_time = #{callTime} " +
            "WHERE id = #{id} AND queue_status = 1 AND window_id = #{windowId}")
    int recall(@Param("id") Long id,
               @Param("windowId") Long windowId,
               @Param("callTime") LocalDateTime callTime,
               @Param("operator") String operator);

    /**
     * 该处方在指定日期是否已有取药号（签到幂等锚点）
     */
    @Select("SELECT * FROM biz_dispense_queue " +
            "WHERE del_flag = 0 AND prescription_id = #{prescriptionId} AND queue_date = #{queueDate} " +
            "ORDER BY id DESC LIMIT 1")
    BizDispenseQueue selectByPrescriptionAndDate(@Param("prescriptionId") Long prescriptionId,
                                                 @Param("queueDate") LocalDate queueDate);

    /**
     * 发药窗口名称（取不到 = 该 id 不是启用的发药窗口）
     */
    @Select("SELECT station_name FROM biz_opd_station " +
            "WHERE id = #{windowId} AND station_kind = 6 AND status = 1 AND del_flag = 0")
    String selectActiveWindowName(@Param("windowId") Long windowId);

    /**
     * 该患者名下还有待发药药品的处方（按处方分组，处方才是取药单元）
     */
    @Select("SELECT prescription_id AS prescriptionId, MAX(prescription_no) AS prescriptionNo, " +
            "       MAX(patient_id) AS patientId, MAX(patient_no) AS patientNo, " +
            "       MAX(patient_name) AS patientName, COUNT(*) AS itemCount " +
            "FROM biz_drug_dispensing " +
            "WHERE del_flag = 0 AND patient_id = #{patientId} AND dispensing_status = 1 " +
            "GROUP BY prescription_id ORDER BY MIN(create_time) ASC")
    List<DispenseWaitingRxVO> selectWaitingRxByPatient(@Param("patientId") Long patientId);
}
