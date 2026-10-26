package com.his.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.platform.entity.SysMedGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 医疗组 / 主诊组 Mapper。
 */
@Mapper
public interface SysMedGroupMapper extends BaseMapper<SysMedGroup> {

    /**
     * 取现有 MG 型医疗组编码的最大序号（MG001 → 1），用于新编码续号。
     */
    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(group_code, 3) AS UNSIGNED)), 0) "
            + "FROM sys_med_group WHERE group_code REGEXP '^MG[0-9]+$' AND del_flag = 0")
    int selectMaxGroupSeq();
}
