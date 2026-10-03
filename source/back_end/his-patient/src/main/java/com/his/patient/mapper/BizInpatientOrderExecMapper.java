package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.OrderExecQueryPageDTO;
import com.his.patient.entity.BizInpatientOrderExec;
import com.his.patient.vo.InpatientOrderExecVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 医嘱执行记录 Mapper。
 *
 * <p>待执行队列（`exec_status = 1`）按 `plan_time` 升序 —— 这是护士"先做哪床"的唯一依据。
 * 只返回**已校对/执行中**的医嘱，未校对的医嘱在护士站不可见（医嘱双人核对的最低要求）。
 */
@Mapper
public interface BizInpatientOrderExecMapper extends BaseMapper<BizInpatientOrderExec> {

    /**
     * 待执行队列（护士站）
     * <p>刻意用 `o.order_status IN (2,3)` 而不是"未被停止"：未校对(1)的医嘱绝不能出现在执行队列里。
     */
    @Select("""
            SELECT e.id, e.order_id, e.admission_id, e.patient_id, e.exec_seq, e.plan_date, e.plan_time,
                   e.exec_time, e.exec_nurse_id, e.exec_nurse_name, e.exec_status, e.exec_note,
                   e.infusion_start_time, e.drip_rate, e.infusion_end_time, e.adverse_flag, e.adverse_note,
                   e.fee_record_id, e.fee_no,
                   o.order_no, o.order_type, o.order_class, o.item_code, o.item_name, o.spec, o.unit,
                   o.dosage, o.dosage_unit, o.route, o.frequency, o.quantity, o.price, o.amount,
                   o.is_urgent, o.order_status, o.patient_name, o.bed_no, o.ward_name, o.doctor_name
            FROM biz_inpatient_order_exec e
                     JOIN biz_inpatient_order o ON o.id = e.order_id AND o.del_flag = 0
            WHERE e.del_flag = 0
              AND e.exec_status = 1
              AND o.order_status IN (2, 3)
              AND (#{q.admissionId} IS NULL OR e.admission_id = #{q.admissionId})
              AND (#{q.patientId} IS NULL OR e.patient_id = #{q.patientId})
              AND (#{q.orderType} IS NULL OR o.order_type = #{q.orderType})
              AND (#{q.orderClass} IS NULL OR o.order_class = #{q.orderClass})
              AND (#{q.isUrgent} IS NULL OR o.is_urgent = #{q.isUrgent})
            ORDER BY o.is_urgent DESC, e.plan_time ASC, e.id ASC
            """)
    IPage<InpatientOrderExecVO> selectPendingPage(IPage<InpatientOrderExecVO> page, @Param("q") OrderExecQueryPageDTO query);

    /**
     * 执行记录查询（含已执行 / 已跳过 / 已退回）
     */
    @Select("""
            SELECT e.id, e.order_id, e.admission_id, e.patient_id, e.exec_seq, e.plan_date, e.plan_time,
                   e.exec_time, e.exec_nurse_id, e.exec_nurse_name, e.exec_status, e.exec_note,
                   e.infusion_start_time, e.drip_rate, e.infusion_end_time, e.adverse_flag, e.adverse_note,
                   e.fee_record_id, e.fee_no,
                   o.order_no, o.order_type, o.order_class, o.item_code, o.item_name, o.spec, o.unit,
                   o.dosage, o.dosage_unit, o.route, o.frequency, o.quantity, o.price, o.amount,
                   o.is_urgent, o.order_status, o.patient_name, o.bed_no, o.ward_name, o.doctor_name
            FROM biz_inpatient_order_exec e
                     JOIN biz_inpatient_order o ON o.id = e.order_id AND o.del_flag = 0
            WHERE e.del_flag = 0
              AND (#{q.admissionId} IS NULL OR e.admission_id = #{q.admissionId})
              AND (#{q.orderId} IS NULL OR e.order_id = #{q.orderId})
              AND (#{q.patientId} IS NULL OR e.patient_id = #{q.patientId})
              AND (#{q.execStatus} IS NULL OR e.exec_status = #{q.execStatus})
              AND (#{q.planDate} IS NULL OR e.plan_date = #{q.planDate})
            ORDER BY e.plan_time DESC, e.id DESC
            """)
    IPage<InpatientOrderExecVO> selectExecPage(IPage<InpatientOrderExecVO> page, @Param("q") OrderExecQueryPageDTO query);

    /**
     * 该医嘱累计已生成多少次计划（用于 exec_seq）
     */
    @Select("SELECT COUNT(*) FROM biz_inpatient_order_exec WHERE del_flag = 0 AND order_id = #{orderId}")
    long countByOrder(@Param("orderId") Long orderId);

    /**
     * 该医嘱在指定日期是否已有计划行（补计划时幂等判定；与唯一索引互为双保险）
     */
    @Select("SELECT COUNT(*) FROM biz_inpatient_order_exec WHERE del_flag = 0 AND order_id = #{orderId} AND plan_date = #{planDate}")
    long countByOrderAndDate(@Param("orderId") Long orderId, @Param("planDate") java.time.LocalDate planDate);

    /**
     * 今日已执行次数（护士站列表展示"今日已执行 N 次"）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_inpatient_order_exec
            WHERE del_flag = 0 AND order_id = #{orderId} AND plan_date = #{planDate} AND exec_status = 2
            """)
    long countExecutedByOrderAndDate(@Param("orderId") Long orderId, @Param("planDate") java.time.LocalDate planDate);

    /**
     * 待执行总数（护士站首页卡片）
     */
    @Select("""
            SELECT COUNT(*)
            FROM biz_inpatient_order_exec e
                     JOIN biz_inpatient_order o ON o.id = e.order_id AND o.del_flag = 0
            WHERE e.del_flag = 0 AND e.exec_status = 1 AND o.order_status IN (2, 3)
            """)
    long countAllPending();

    /**
     * 全院在院患者的待执行条数（按入院过滤）
     */
    @Select("""
            SELECT COUNT(*)
            FROM biz_inpatient_order_exec e
                     JOIN biz_inpatient_order o ON o.id = e.order_id AND o.del_flag = 0
            WHERE e.del_flag = 0 AND e.exec_status = 1 AND o.order_status IN (2, 3)
              AND (#{admissionId} IS NULL OR e.admission_id = #{admissionId})
            """)
    long countPendingByAdmission(@Param("admissionId") Long admissionId);
}
