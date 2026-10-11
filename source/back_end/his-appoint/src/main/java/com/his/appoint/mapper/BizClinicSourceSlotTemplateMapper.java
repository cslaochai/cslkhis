package com.his.appoint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.appoint.entity.BizClinicSourceSlotTemplate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 排班模板时间片段Mapper（模板下的半小时段配额）
 */
@Mapper
public interface BizClinicSourceSlotTemplateMapper extends BaseMapper<BizClinicSourceSlotTemplate> {

    /**
     * 物理删模板下的全部段配置。唯一键 uk_tpl_slot(template_id, start_time) 不含 del_flag，
     * 整批替换若走软删，留下的行仍占着键，重插同起点段必然 Duplicate entry。
     */
    @Delete("DELETE FROM biz_clinic_source_slot_template WHERE template_id = #{templateId}")
    int purgeByTemplateId(@Param("templateId") Long templateId);
}
