package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizPathwayStep;
import com.his.emr.vo.PathwayStepVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 临床路径步骤 Mapper。
 */
@Mapper
public interface BizPathwayStepMapper extends BaseMapper<BizPathwayStep> {

    @Select("""
            SELECT s.id, s.pathway_id, s.day_no, s.item_type, s.item_name, s.item_code, s.content, s.sort_no
              FROM biz_pathway_step s
             WHERE s.del_flag = 0 AND s.pathway_id = #{pathwayId}
             ORDER BY s.day_no ASC, s.sort_no ASC, s.id ASC
            """)
    List<PathwayStepVO> selectStepsByPathwayId(@Param("pathwayId") Long pathwayId);

    /**
     * 发布前按步骤回算路径总日数（无步骤返回 NULL）
     */
    @Select("SELECT MAX(day_no) FROM biz_pathway_step WHERE del_flag = 0 AND pathway_id = #{pathwayId}")
    Integer selectMaxDayNo(@Param("pathwayId") Long pathwayId);
}
