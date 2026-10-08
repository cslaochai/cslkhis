package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizRxReviewBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BizRxReviewBatchMapper extends BaseMapper<BizRxReviewBatch> {

    /**
     * 建批抽样：就诊日期范围内、真实发生（已审核/已发药）且**未被任何批次收录**的处方，
     * 随机抽 limit 张。点评对象排除 5-已取消 / 6-已退药 / 7-审方退回（没有真实用药，点评无意义）。
     *
     * <p>NOT EXISTS 排除已收录 —— 一张处方只进一个批次，点评率不重复计。
     */
    @Select("""
            SELECT p.* FROM biz_prescription p
            WHERE p.del_flag = 0
              AND p.prescription_status IN (3, 4)
              AND p.visit_date BETWEEN #{dateStart} AND #{dateEnd}
              AND NOT EXISTS (SELECT 1 FROM biz_rx_review_item i WHERE i.prescription_id = p.id)
            ORDER BY RAND()
            LIMIT #{limit}
            """)
    List<BizPrescription> samplePrescriptions(@Param("dateStart") LocalDate dateStart,
                                              @Param("dateEnd") LocalDate dateEnd,
                                              @Param("limit") int limit);

    /**
     * 已点评数原子 +1（读-改-写并发下会少计，直接 SQL 自增）
     */
    @Update("UPDATE biz_rx_review_batch SET reviewed_count = reviewed_count + 1 WHERE id = #{id}")
    int increaseReviewed(@Param("id") Long id);
}
