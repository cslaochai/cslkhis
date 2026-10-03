package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizEcgTemplate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizEcgTemplateMapper extends BaseMapper<BizEcgTemplate> {

    /**
     * 物理删模板：uk_ecg_tpl_code 不含 del_flag，软删的行仍占着唯一键，
     * 同编码的模板就再也建不出来（sql/138 同款口径）。
     * 纯配置表没有留档价值，删除必须物理删。
     */
    @Delete("DELETE FROM biz_ecg_template WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
