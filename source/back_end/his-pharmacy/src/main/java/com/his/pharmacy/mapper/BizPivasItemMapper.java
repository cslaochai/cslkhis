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
 *
 * <p>候选医嘱捞取是跨模块读（住院医嘱主表 × 入院记录 × 药品字典），走裸 SQL。
 *
 * <p>候选口径 = 住院摆药同口径（在院、药品类医嘱（医嘱类别 1）、状态 2/3、当日在给药期、
 * 按医嘱项目编码命中药品编码找到药品档案），再叠加给药途径的静脉关键词
 *
 * <p><b>为什么「临床营养（医嘱类别 10）」不进静配池 —— 这是定好的口径，不是漏配：</b>
 * 10 是「营养治疗方案」这一层（如"肠外营养支持：热量 1800kcal / 氮 12g"），
 * 静配中心调配的是**具体输液品种**（三腔袋 / 复方氨基酸 / 脂肪乳），那些照药品类医嘱（医嘱类别 1）开，
 * 名称与编码走药品字典，因此天然命中本候选。
 * 反过来，把 10 塞进候选也没用：10 没有药品字典、走手输（项目编码是自定义方案码如 `CN-PN-001`），
 * 拿项目编码去匹配药品字典的药品编码命中不了 —— 放宽医嘱类别只会让
 * 「生成结果 0 条」变成「看不出为什么 0 条」，不会多调配出任何一袋。
 * 环境里 4 条类别 10 的营养医嘱（2026-09-28 核对）项目编码均为 CN-* 自定义码，即此。
 * （静滴/静注/静推/静脉/泵入，与 InpatientInfusionServiceImpl.INFUSION_KEYWORDS 一致；
 * 给药途径存中文原文无字典，只能关键词命中）。
 * 排除当日已有**未拒配**静配明细的医嘱（审方退回 0 允许重新入单）。
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
