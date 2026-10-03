package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizRxReviewItem;
import com.his.emr.vo.RxPublicityDoctorVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface BizRxReviewItemMapper extends BaseMapper<BizRxReviewItem> {

    /** 删批次 = 整批物理删明细（uk_batch_rx 不含 del_flag，软删会占键）。调用前必须校验批次下无已点评明细。 */
    @Delete("DELETE FROM biz_rx_review_item WHERE batch_id = #{batchId}")
    int purgeByBatchId(@Param("batchId") Long batchId);

    /**
     * 月度点评统计（按明细快照 visit_date 聚合）。
     * SUM(布尔) 依赖 MySQL 把 true/false 当 1/0。
     */
    @Select("""
            SELECT COUNT(*)                                   AS reviewed,
                   SUM(review_result IN (2, 3, 4))            AS unreasonable,
                   SUM(review_result = 4)                     AS abnormal,
                   SUM(publicity_status = 1)                  AS publicized,
                   SUM(review_status = 0)                     AS pending
            FROM biz_rx_review_item
            WHERE visit_date BETWEEN #{dateStart} AND #{dateEnd}
            """)
    Map<String, Object> selectMonthlyStats(@Param("dateStart") LocalDate dateStart,
                                           @Param("dateEnd") LocalDate dateEnd);

    /**
     * 处方总数（同期已审核/已发药处方量，点评率的分母）。
     * 裸 SQL 不受 @TableLogic 影响 → 显式 del_flag = 0。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_prescription
            WHERE del_flag = 0
              AND prescription_status IN (3, 4)
              AND visit_date BETWEEN #{dateStart} AND #{dateEnd}
            """)
    long countPrescriptions(@Param("dateStart") LocalDate dateStart,
                            @Param("dateEnd") LocalDate dateEnd);

    /**
     * 公示页医师排名（只统计**已公示**的不合理处方：公示过的曝光才有警示意义）。
     * 超常 ≥3 次的医师在 service 标记 needTalk（规范：警告并限制处方权的触发线）。
     */
    @Select("""
            SELECT i.doctor_id                AS doctorId,
                   i.doctor_name              AS doctorName,
                   MAX(i.dept_name)           AS deptName,
                   COUNT(*)                   AS reviewCount,
                   SUM(i.review_result = 4)   AS abnormalCount,
                   COUNT(*)                   AS unreasonableCount,
                   MAX(i.publicity_time)      AS lastPublicityTime
            FROM biz_rx_review_item i
            WHERE i.review_status = 1
              AND i.review_result IN (2, 3, 4)
              AND i.publicity_status = 1
              AND (#{dateStart} IS NULL OR i.visit_date >= #{dateStart})
              AND (#{dateEnd} IS NULL OR i.visit_date <= #{dateEnd})
            GROUP BY i.doctor_id, i.doctor_name
            ORDER BY abnormalCount DESC, unreasonableCount DESC, i.doctor_id
            LIMIT 200
            """)
    List<RxPublicityDoctorVO> selectPublicityDoctorStats(@Param("dateStart") LocalDate dateStart,
                                                         @Param("dateEnd") LocalDate dateEnd);
}
