package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizCssdPack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * CSSD 器械包 Mapper。
 */
@Mapper
public interface BizCssdPackMapper extends BaseMapper<BizCssdPack> {

    /**
     * 条码占用查重（含软删行——唯一键不认 del_flag，漏了软删行会撞唯一键 500）
     */
    @Select("SELECT id FROM biz_cssd_pack WHERE pack_no = #{no} LIMIT 1")
    Long selectIdByNoAny(String no);
}
