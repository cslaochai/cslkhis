package com.his.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.ai.entity.SysAiQaColumn;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * AI 运营问数数据列配置 Mapper。
 */
@Mapper
public interface SysAiQaColumnMapper extends BaseMapper<SysAiQaColumn> {

    /**
     * 物理删：列配置无留档价值；软删旧行占唯一键 uk_table_column(table_name, column_name, del_flag)，
     * 同名列「删→重建→再删」会撞键（G12 已踩）。
     */
    @Delete("DELETE FROM sys_ai_qa_column WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    /**
     * 物理删某表的全部列配置（随表删除级联，同 purgeById 口径）。
     */
    @Delete("DELETE FROM sys_ai_qa_column WHERE table_name = #{tableName}")
    int purgeByTable(@Param("tableName") String tableName);
}
