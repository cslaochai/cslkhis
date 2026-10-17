package com.his.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.ai.entity.SysAiQaSchema;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AI 运营问数数据表配置 Mapper。
 */
@Mapper
public interface SysAiQaSchemaMapper extends BaseMapper<SysAiQaSchema> {

    /**
     * 物理删：表配置无留档价值；软删旧行占唯一键 uk_table_name(table_name, del_flag)，
     * 同名表「删→重建→再删」会撞键（G12 已踩）。
     */
    @Delete("DELETE FROM sys_ai_qa_schema WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
