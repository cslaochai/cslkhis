package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizPivasItem;
import com.his.pharmacy.vo.PivasCandidateVO;
import com.his.pharmacy.vo.PivasItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 静配中心调配明细 Mapper。
 */
@Mapper
public interface BizPivasItemMapper extends BaseMapper<BizPivasItem> {

    /**
     * route 静脉关键词条件（静配与摆药口径的唯一差异）
     */
    String IV_ROUTE_COND = " AND (o.route LIKE '%静滴%' OR o.route LIKE '%静注%' OR o.route LIKE '%静推%'"
            + " OR o.route LIKE '%静脉%' OR o.route LIKE '%泵入%') ";

    /**
     * 可入静配单的医嘱候选
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
                   o.route         AS route,
                   o.frequency     AS frequency,
                   d.id            AS drugId,
                   d.drug_name     AS drugName
              FROM biz_inpatient_order o
              JOIN biz_admission a
                ON a.admission_id = o.admission_id AND a.admit_status = 1 AND a.del_flag = 0
              JOIN sys_drug d
                ON d.drug_code = o.item_code AND d.del_flag = 0
              LEFT JOIN biz_pivas_item i
                ON i.order_id = o.id AND i.admix_date = #{admixDate}
               AND i.del_flag = 0 AND i.status != 0
             WHERE o.del_flag = 0
               AND o.order_class = 1
               AND o.order_status IN (2, 3)
               AND o.ward_id = #{wardId}
               AND o.start_time &lt; #{dayEnd}
               AND (o.stop_time IS NULL OR o.stop_time &gt;= #{dayStart})
               AND (o.plan_end_time IS NULL OR o.plan_end_time &gt;= #{dayStart})
               AND i.id IS NULL
            """ + IV_ROUTE_COND + """
               <if test="admissionId != null"> AND o.admission_id = #{admissionId}</if>
             ORDER BY o.id
            </script>
            """)
    List<PivasCandidateVO> selectCandidates(@Param("wardId") Long wardId,
                                            @Param("admixDate") LocalDate admixDate,
                                            @Param("dayStart") LocalDateTime dayStart,
                                            @Param("dayEnd") LocalDateTime dayEnd,
                                            @Param("admissionId") Long admissionId);

    /**
     * 同口径但静脉药未能匹配药品档案的医嘱数（生成结果反馈"N 条无法匹配药品档案，已跳过"）
     */
    @Select("""
            <script>
            SELECT COUNT(*)
              FROM biz_inpatient_order o
              JOIN biz_admission a
                ON a.admission_id = o.admission_id AND a.admit_status = 1 AND a.del_flag = 0
              LEFT JOIN sys_drug d
                ON d.drug_code = o.item_code AND d.del_flag = 0
              LEFT JOIN biz_pivas_item i
                ON i.order_id = o.id AND i.admix_date = #{admixDate}
               AND i.del_flag = 0 AND i.status != 0
             WHERE o.del_flag = 0
               AND o.order_class = 1
               AND o.order_status IN (2, 3)
               AND o.ward_id = #{wardId}
               AND o.start_time &lt; #{dayEnd}
               AND (o.stop_time IS NULL OR o.stop_time &gt;= #{dayStart})
               AND (o.plan_end_time IS NULL OR o.plan_end_time &gt;= #{dayStart})
               AND i.id IS NULL
               AND d.id IS NULL
            """ + IV_ROUTE_COND + """
               <if test="admissionId != null"> AND o.admission_id = #{admissionId}</if>
            </script>
            """)
    long countUnmatchedCandidates(@Param("wardId") Long wardId,
                                  @Param("admixDate") LocalDate admixDate,
                                  @Param("dayStart") LocalDateTime dayStart,
                                  @Param("dayEnd") LocalDateTime dayEnd,
                                  @Param("admissionId") Long admissionId);

    /**
     * 同医嘱同日的下一个重排入序号（含已拒配明细；并发冲突由唯一键兜底）
     */
    @Select("""
            SELECT COALESCE(MAX(i.pivas_seq), 0) + 1
              FROM biz_pivas_item i
             WHERE i.order_id = #{orderId} AND i.admix_date = #{admixDate} AND i.del_flag = 0
            """)
    int nextPivasSeq(@Param("orderId") Long orderId, @Param("admixDate") LocalDate admixDate);

    /**
     * 调配日内的最大排队号（打标签取号起点，中心叫号口径跨病区连续）
     */
    @Select("""
            SELECT COALESCE(MAX(i.queue_no), 0)
              FROM biz_pivas_item i
             WHERE i.admix_date = #{admixDate} AND i.del_flag = 0
            """)
    int maxQueueNo(@Param("admixDate") LocalDate admixDate);

    /**
     * 按主单查明细（排队号优先，让配台顺序与台账一致；id 二级排序防抖动）
     */
    @Select("""
            SELECT i.* FROM biz_pivas_item i
             WHERE i.pivas_id = #{pivasId} AND i.del_flag = 0
             ORDER BY (i.queue_no IS NULL), i.queue_no, i.id
            """)
    List<PivasItemVO> selectItemsByBatchId(@Param("pivasId") Long pivasId);
}
