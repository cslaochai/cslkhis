package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 放射报告模板 Mapper。
 *
 * <p>uk_template_code 不含 del_flag（sql/138）→ 删除走**物理删**，
 * 模板是配置数据没有留档价值，软删留下的行会一直占着编码。
 */
@Mapper
public interface BizRadioReportTemplateMapper extends BaseMapper<com.his.medicaltech.entity.BizRadioReportTemplate> {

    /**
     * 物理删（唯一键不含 del_flag，软删会让同编码模板再也建不出来）。
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM biz_radio_report_template WHERE id = #{id}")
    int purgeById(@org.apache.ibatis.annotations.Param("id") Long id);
}
