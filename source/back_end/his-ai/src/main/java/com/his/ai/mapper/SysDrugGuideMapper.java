package com.his.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.ai.entity.SysDrugGuide;
import org.apache.ibatis.annotations.Select;

/**
 * 药品字典只读查询（用药说明用）。
 *
 * <p>只按药品ID/编码取单条，不做任何写操作；查不到返回 null，
 * 调用方按「无说明书信息」处理（用药说明的事实层来自处方医嘱，说明书只是补充）。
 */
public interface SysDrugGuideMapper extends BaseMapper<SysDrugGuide> {

    /**
     * 按药品ID取（del_flag=0，裸 SQL 必须带软删条件）
     */
    @Select("SELECT id, drug_code, drug_name, specification, dosage_form, unit, storage_condition, "
            + "is_cold_chain, special_flag, antibiotic_level, usage_dosage "
            + "FROM sys_drug WHERE id = #{drugId} AND del_flag = 0 LIMIT 1")
    SysDrugGuide selectGuideById(Long drugId);

    /**
     * 按药品编码取（处方明细的 drug_id 缺失时兜底）
     */
    @Select("SELECT id, drug_code, drug_name, specification, dosage_form, unit, storage_condition, "
            + "is_cold_chain, special_flag, antibiotic_level, usage_dosage "
            + "FROM sys_drug WHERE drug_code = #{drugCode} AND del_flag = 0 LIMIT 1")
    SysDrugGuide selectGuideByCode(String drugCode);
}
