package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.SysSingleDisease;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/** 单病种目录 Mapper */
@Mapper
public interface SysSingleDiseaseMapper extends BaseMapper<SysSingleDisease> {

    /**
     * 物理删除：{@code disease_code} 唯一键不含 del_flag，软删行会占住唯一键
     * （AGENTS.md 铁律：唯一键不含 del_flag 的配置表删除必须物理删）。
     */
    @Delete("DELETE FROM sys_single_disease WHERE id = #{id}")
    int purgeById(Long id);
}
