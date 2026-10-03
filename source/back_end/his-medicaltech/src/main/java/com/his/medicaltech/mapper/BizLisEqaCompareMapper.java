package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizLisEqaCompare;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 仪器间比对 Mapper
 */
@Mapper
public interface BizLisEqaCompareMapper extends BaseMapper<BizLisEqaCompare> {

    /**
     * 按批次物理删干净互差。
     *
     * <p>每次成绩回报都要整体重算本批次的互差；uk_compare 唯一键不含 del_flag，
     * 所以这里的清除必须物理删 —— 用 BaseMapper 的 delete（软删）会在回插时撞 Duplicate entry。
     */
    @Delete("DELETE FROM biz_lis_eqa_compare WHERE plan_id = #{planId}")
    int purgeByPlanId(@Param("planId") Long planId);
}
