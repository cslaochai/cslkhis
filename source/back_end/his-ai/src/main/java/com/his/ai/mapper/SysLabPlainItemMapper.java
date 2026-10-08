package com.his.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.ai.entity.SysLabPlainItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 检验项目白话词典。his-ai 自己的表，直接走 MP。
 */
@Mapper
public interface SysLabPlainItemMapper extends BaseMapper<SysLabPlainItem> {

    /**
     * 物理删除。
     */
    @Delete("DELETE FROM sys_lab_plain_item WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    /**
     * 按项目名精确查一行（不排除软删行 —— 唯一键不含 del_flag，软删的同名行一样会撞键）。
     */
    @Select("SELECT * FROM sys_lab_plain_item WHERE item_name = #{itemName} LIMIT 1")
    SysLabPlainItem selectByItemName(@Param("itemName") String itemName);

    /**
     * 现有分组清单（给维护页筛选下拉用），按条目数倒序。
     */
    @Select("SELECT group_name FROM sys_lab_plain_item "
            + "WHERE del_flag = 0 AND group_name IS NOT NULL AND group_name <> '' "
            + "GROUP BY group_name ORDER BY COUNT(*) DESC, group_name")
    List<String> selectGroupNames();
}
