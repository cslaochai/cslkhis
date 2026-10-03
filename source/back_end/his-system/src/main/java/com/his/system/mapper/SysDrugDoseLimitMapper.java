package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysDrugDoseLimit;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 药品剂量上限知识库 Mapper
 */
@Mapper
public interface SysDrugDoseLimitMapper extends BaseMapper<SysDrugDoseLimit> {

    /**
     * ⚠ 物理删：uk_dose_component 不含 del_flag，软删的行仍占着成分名这个唯一键，
     * 删了再建同一成分必然 Duplicate entry（AGENTS §3，见 sql/130 头注）。
     */
    @Delete("DELETE FROM sys_drug_dose_limit WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
