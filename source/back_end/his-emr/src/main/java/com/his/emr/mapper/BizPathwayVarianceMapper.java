package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizPathwayVariance;
import com.his.emr.vo.PathwayAnalysisVO;
import com.his.emr.vo.PathwayVarianceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 临床路径变异登记 Mapper（含变异分析聚合）。
 */
@Mapper
public interface BizPathwayVarianceMapper extends BaseMapper<BizPathwayVariance> {

    @Select("""
            SELECT v.* FROM biz_pathway_variance v
             WHERE v.del_flag = 0 AND v.enroll_id = #{enrollId}
             ORDER BY v.day_no ASC, v.id ASC
            """)
    List<PathwayVarianceVO> selectVariancesByEnrollId(@Param("enrollId") Long enrollId);

    /**
     * 分析主表：按模板聚合入径数/完成/退径/变异数（pathwayId 为空则全院）
     */
    @Select("""
            <script>
            SELECT e.pathway_id   AS pathwayId,
                   e.pathway_code AS pathwayCode,
                   e.pathway_name AS pathwayName,
                   e.version      AS version,
                   COUNT(*)                                          AS enrollCount,
                   SUM(CASE WHEN e.status = 2 THEN 1 ELSE 0 END)     AS finishCount,
                   SUM(CASE WHEN e.status = 3 THEN 1 ELSE 0 END)     AS abortCount,
                   SUM(e.variance_count)                             AS varianceCount
              FROM biz_pathway_enroll e
             WHERE e.del_flag = 0
               <if test="pathwayId != null"> AND e.pathway_id = #{pathwayId}</if>
             GROUP BY e.pathway_id, e.pathway_code, e.pathway_name, e.version
             ORDER BY enrollCount DESC, e.pathway_id DESC
            </script>
            """)
    List<PathwayAnalysisVO.Row> selectEnrollStats(@Param("pathwayId") Long pathwayId);

    /**
     * 变异类型分布
     */
    @Select("""
            <script>
            SELECT v.variance_type AS varianceType, COUNT(*) AS cnt
              FROM biz_pathway_variance v
              JOIN biz_pathway_enroll e ON e.id = v.enroll_id AND e.del_flag = 0
             WHERE v.del_flag = 0
               <if test="pathwayId != null"> AND e.pathway_id = #{pathwayId}</if>
             GROUP BY v.variance_type
             ORDER BY cnt DESC, v.variance_type ASC
            </script>
            """)
    List<PathwayAnalysisVO.TypeStat> selectVarianceTypeStats(@Param("pathwayId") Long pathwayId);

    /**
     * 变异原因 TOP
     */
    @Select("""
            <script>
            SELECT v.variance_reason AS reason, COUNT(*) AS cnt
              FROM biz_pathway_variance v
              JOIN biz_pathway_enroll e ON e.id = v.enroll_id AND e.del_flag = 0
             WHERE v.del_flag = 0
               <if test="pathwayId != null"> AND e.pathway_id = #{pathwayId}</if>
             GROUP BY v.variance_reason
             ORDER BY cnt DESC, MAX(v.id) DESC
             LIMIT #{limit}
            </script>
            """)
    List<PathwayAnalysisVO.ReasonStat> selectTopReasons(@Param("pathwayId") Long pathwayId,
                                                        @Param("limit") int limit);
}
