package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.patient.dto.NutritionStatsQueryPageDTO;
import com.his.patient.entity.BizNutritionStats;
import com.his.patient.vo.NutritionStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 营养膳食月度指标 Mapper。
 *
 * <p>本表无 del_flag（快照覆盖式），删除只用于"重算前清空某月"，走 {@code DELETE}。
 */
@Mapper
public interface BizNutritionStatsMapper extends BaseMapper<BizNutritionStats> {

    @Select("""
            <script>
            SELECT t.*,
                   CASE t.scope_type WHEN 1 THEN '全院' WHEN 2 THEN '科室' ELSE '未知' END AS scope_type_text
              FROM biz_nutrition_stats t
             WHERE 1 = 1
               AND (#{q.statMonth} IS NULL OR #{q.statMonth} = '' OR t.stat_month = #{q.statMonth})
               AND (#{q.scopeType} IS NULL OR t.scope_type = #{q.scopeType})
               AND (#{q.deptId} IS NULL OR t.dept_id = #{q.deptId})
             ORDER BY t.stat_month DESC, t.scope_type ASC, t.dept_id ASC
            </script>
            """)
    IPage<NutritionStatsVO> selectStatsPage(Page<NutritionStatsVO> page,
                                            @Param("q") NutritionStatsQueryPageDTO query);

    /** 导出用（不分页，上限由服务层控制） */
    @Select("""
            <script>
            SELECT t.*,
                   CASE t.scope_type WHEN 1 THEN '全院' WHEN 2 THEN '科室' ELSE '未知' END AS scope_type_text
              FROM biz_nutrition_stats t
             WHERE 1 = 1
               AND (#{q.statMonth} IS NULL OR #{q.statMonth} = '' OR t.stat_month = #{q.statMonth})
               AND (#{q.scopeType} IS NULL OR t.scope_type = #{q.scopeType})
               AND (#{q.deptId} IS NULL OR t.dept_id = #{q.deptId})
             ORDER BY t.stat_month DESC, t.scope_type ASC, t.dept_id ASC
             LIMIT 5000
            </script>
            """)
    List<NutritionStatsVO> selectStatsForExport(@Param("q") NutritionStatsQueryPageDTO query);

    /** 某月某范围的已有快照行（覆盖式 upsert 前定位用） */
    @Select("""
            SELECT * FROM biz_nutrition_stats
             WHERE stat_month = #{statMonth} AND scope_type = #{scopeType}
               AND (#{deptId} IS NULL OR dept_id = #{deptId})
               AND (#{deptId} IS NOT NULL OR dept_id IS NULL)
             LIMIT 1
            """)
    BizNutritionStats selectOneSnapshot(@Param("statMonth") String statMonth,
                                        @Param("scopeType") Integer scopeType,
                                        @Param("deptId") Long deptId);
}
