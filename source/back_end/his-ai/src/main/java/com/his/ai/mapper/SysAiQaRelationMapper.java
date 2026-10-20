package com.his.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.ai.entity.SysAiQaRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AI 运营问数表间关系配置 Mapper。
 */
@Mapper
public interface SysAiQaRelationMapper extends BaseMapper<SysAiQaRelation> {

    /**
     * 物理删：关系配置无留档价值；唯一键 uk_rel_src_tgt(表×2+列×2+del_flag) 下
     * 软删旧行仍占位，同关系「删→重建→再删」会撞键（同 G12 语义配置的坑）。
     */
    @Delete("DELETE FROM sys_ai_qa_relation WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
