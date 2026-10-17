package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizStaffPlanRule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 人力配置标准 Mapper。
 */
@Mapper
public interface BizStaffPlanRuleMapper extends BaseMapper<BizStaffPlanRule> {

    /**
     * 物理删单行：本表唯一键「单元 × 班次 × 岗位类别」不含删除标志，
     * 软删后按同一口径重建必撞重复键。
     */
    @Delete("DELETE FROM biz_staff_plan_rule WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
