package com.his.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.ai.entity.SysAiQaSemantic;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AI 运营问数语义映射配置 Mapper。
 */
@Mapper
public interface SysAiQaSemanticMapper extends BaseMapper<SysAiQaSemantic> {

    /**
     * 物理删：语义配置无留档价值；且唯一键 uk_term(term, del_flag) 下软删旧行仍占位，
     * 同名词条「删→重建→再删」会把新行软删成 (term,1) 与旧行撞键（G12 已踩）。
     */
    @Delete("DELETE FROM sys_ai_qa_semantic WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
