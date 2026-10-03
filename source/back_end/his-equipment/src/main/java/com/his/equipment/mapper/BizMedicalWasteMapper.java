package com.his.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.equipment.entity.BizMedicalWaste;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 医疗废物登记 Mapper。
 */
@Mapper
public interface BizMedicalWasteMapper extends BaseMapper<BizMedicalWaste> {

    /** 单号占用查重（含软删行——唯一键不认 del_flag，漏了软删行会撞唯一键 500） */
    @Select("SELECT id FROM biz_medical_waste WHERE waste_no = #{no} LIMIT 1")
    Long selectIdByNoAny(String no);
}
