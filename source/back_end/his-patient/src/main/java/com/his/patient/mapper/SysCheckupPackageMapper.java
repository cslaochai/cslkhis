package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.SysCheckupPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 体检套餐 Mapper。套餐名唯一键含软删行，查重走不滤 del_flag 的裸 SQL。
 */
@Mapper
public interface SysCheckupPackageMapper extends BaseMapper<SysCheckupPackage> {

    /** 含软删行查重（唯一键 uk_package_name 不看 del_flag） */
    @Select("SELECT id FROM sys_checkup_package WHERE package_name = #{name} LIMIT 1")
    Long selectIdByNameIncludeDeleted(@Param("name") String name);
}
