package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 放射报告模板 Mapper。
 */
@Mapper
public interface BizRadioReportTemplateMapper extends BaseMapper<com.his.medicaltech.entity.BizRadioReportTemplate> {

    /**
     * 物理删（唯一键不含 del_flag，软删会让同编码模板再也建不出来）。
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM biz_radio_report_template WHERE id = #{id}")
    int purgeById(@org.apache.ibatis.annotations.Param("id") Long id);
}
