package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizWardDispenseItem;
import com.his.pharmacy.vo.WardDispenseCandidateVO;
import com.his.pharmacy.vo.WardDispenseOrderDeptVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院摆药明细 Mapper。
 */
@Mapper
public interface BizWardDispenseItemMapper extends BaseMapper<BizWardDispenseItem> {

    /**
     * 可摆药医嘱候选
     */
    @Select("""
            <script>
            SELECT o.id            AS orderId,
                   o.order_no      AS orderNo,
                   o.admission_id  AS admissionId,
                   o.patient_id    AS patientId,
                   o.patient_no    AS patientNo,
                   o.patient_name  AS patientName,
                   o.ward_id       AS wardId,
                   o.order_type    AS orderType,
                   o.item_code     AS itemCode,
                   o.item_name     AS itemName,
                   o.spec          AS spec,
                   o.unit          AS unit,
                   o.quantity      AS quantity,
                   o.price         AS price,
                   d.id            AS drugId,
                   d.drug_name     AS drugName
              FROM biz_inpatient_order o
              JOIN biz_admission a
                ON a.admission_id = o.admission_id AND a.admit_status = 1 AND a.del_flag = 0
              JOIN sys_drug d
                ON d.drug_code = o.item_code AND d.del_flag = 0
              LEFT JOIN biz_ward_dispense_item i
                ON i.order_id = o.id AND i.dispense_date = #{dispenseDate}
               AND i.del_flag = 0 AND i.status != 4
             WHERE o.del_flag = 0
               AND o.order_class = 1
               AND o.order_status IN (2, 3)
               AND o.ward_id = #{wardId}
               AND o.start_time &lt; #{dayEnd}
               AND (o.stop_time IS NULL OR o.stop_time &gt;= #{dayStart})
               AND (o.plan_end_time IS NULL OR o.plan_end_time &gt;= #{dayStart})
               AND i.id IS NULL
               <if test="admissionId != null"> AND o.admission_id = #{admissionId}</if>
             ORDER BY o.id
            </script>
            """)
    List<WardDispenseCandidateVO> selectCandidates(@Param("wardId") Long wardId,
                                                   @Param("dispenseDate") LocalDate dispenseDate,
                                                   @Param("dayStart") LocalDateTime dayStart,
                                                   @Param("dayEnd") LocalDateTime dayEnd,
                                                   @Param("admissionId") Long admissionId);

    /**
     * 同口径但未能匹配药品档案的医嘱数（生成结果反馈"N 条无法匹配药品档案，已跳过"）
     */
    @Select("""
            <script>
            SELECT COUNT(*)
              FROM biz_inpatient_order o
              JOIN biz_admission a
                ON a.admission_id = o.admission_id AND a.admit_status = 1 AND a.del_flag = 0
              LEFT JOIN sys_drug d
                ON d.drug_code = o.item_code AND d.del_flag = 0
              LEFT JOIN biz_ward_dispense_item i
                ON i.order_id = o.id AND i.dispense_date = #{dispenseDate}
               AND i.del_flag = 0 AND i.status != 4
             WHERE o.del_flag = 0
               AND o.order_class = 1
               AND o.order_status IN (2, 3)
               AND o.ward_id = #{wardId}
               AND o.start_time &lt; #{dayEnd}
               AND (o.stop_time IS NULL OR o.stop_time &gt;= #{dayStart})
               AND (o.plan_end_time IS NULL OR o.plan_end_time &gt;= #{dayStart})
               AND i.id IS NULL
               AND d.id IS NULL
               <if test="admissionId != null"> AND o.admission_id = #{admissionId}</if>
            </script>
            """)
    long countUnmatchedCandidates(@Param("wardId") Long wardId,
                                  @Param("dispenseDate") LocalDate dispenseDate,
                                  @Param("dayStart") LocalDateTime dayStart,
                                  @Param("dayEnd") LocalDateTime dayEnd,
                                  @Param("admissionId") Long admissionId);

    /**
     * 同医嘱同日的下一个重摆序号（含已退药明细；并发冲突由唯一键兜底）
     */
    @Select("""
            SELECT COALESCE(MAX(i.dispense_seq), 0) + 1
              FROM biz_ward_dispense_item i
             WHERE i.order_id = #{orderId} AND i.dispense_date = #{dispenseDate} AND i.del_flag = 0
            """)
    int nextDispenseSeq(@Param("orderId") Long orderId, @Param("dispenseDate") LocalDate dispenseDate);

    /**
     * 按医嘱号取开立科室（同样是跨模块读，走裸 SQL）
     */
    @Select("""
            SELECT o.dept_id AS deptId, o.dept_name AS deptName
              FROM biz_inpatient_order o
             WHERE o.order_no = #{orderNo} AND o.del_flag = 0
             ORDER BY o.id
             LIMIT 1
            """)
    WardDispenseOrderDeptVO selectOrderDept(@Param("orderNo") String orderNo);

    /**
     * 按主单查明细（id 二级排序，防翻页抖动）
     */
    @Select("""
            SELECT i.* FROM biz_ward_dispense_item i
             WHERE i.dispense_id = #{dispenseId} AND i.del_flag = 0
             ORDER BY i.id
            """)
    List<com.his.pharmacy.vo.WardDispenseItemVO> selectItemsByDispenseId(@Param("dispenseId") Long dispenseId);
}
