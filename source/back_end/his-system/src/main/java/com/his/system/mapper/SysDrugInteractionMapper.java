package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysDrugInteraction;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 药物相互作用知识库 Mapper
 */
@Mapper
public interface SysDrugInteractionMapper extends BaseMapper<SysDrugInteraction> {

    /**
     * ⚠ 物理删：uk_pair_key 不含 del_flag，软删的行仍占着「同一对成分」这个唯一键，
     * 删了再建同一对必然 Duplicate entry（AGENTS §3，见 sql/130 头注）。
     * 只想让条目暂时失效请改 status=0，不要走删除。
     */
    @Delete("DELETE FROM sys_drug_interaction WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
