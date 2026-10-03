package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 字典数据Mapper
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    @Select("SELECT * FROM sys_dict_data WHERE dict_type = #{dictType} AND status = 1 AND del_flag = 0 ORDER BY dict_sort")
    List<SysDictData> selectByDictType(@Param("dictType") String dictType);
}
