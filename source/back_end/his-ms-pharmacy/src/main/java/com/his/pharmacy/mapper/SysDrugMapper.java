package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.SysDrug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 药品Mapper
 */
@Mapper
public interface SysDrugMapper extends BaseMapper<SysDrug> {

    /**
     * 按成分关键字统计在用药品的条数（药品名称或通用名包含即算）
     * <p>合理用药知识库用它区分「知识写错了」和「本院还没进这个药」，见 sql/130 自检 T5。
     * 裸 SQL 需自带 del_flag（@TableLogic 只作用于 Wrapper 查询）。
     */
    @Select("SELECT COUNT(*) FROM sys_drug WHERE del_flag = 0 AND status = 1"
            + " AND (drug_name LIKE CONCAT('%', #{keyword}, '%') OR generic_name LIKE CONCAT('%', #{keyword}, '%'))")
    int countByComponent(@Param("keyword") String keyword);

    /**
     * 按药品ID取「用药说明」要读的列（患者端服用方法用）。
     * <p>只取储存、管制、抗菌分级、说明书用法用量这几列，不把整张字典拉出来。
     * 裸 SQL 需自带 del_flag（@TableLogic 只作用于 Wrapper 查询）。
     */
    @Select("SELECT id, drug_code, drug_name, specification, dosage_form, unit, storage_condition, "
            + "is_cold_chain, special_flag, antibiotic_level, usage_dosage "
            + "FROM sys_drug WHERE id = #{drugId} AND del_flag = 0 LIMIT 1")
    SysDrug selectGuideById(@Param("drugId") Long drugId);

    /**
     * 按药品编码取用药说明列（处方明细的药品ID缺失时兜底）。
     */
    @Select("SELECT id, drug_code, drug_name, specification, dosage_form, unit, storage_condition, "
            + "is_cold_chain, special_flag, antibiotic_level, usage_dosage "
            + "FROM sys_drug WHERE drug_code = #{drugCode} AND del_flag = 0 LIMIT 1")
    SysDrug selectGuideByCode(@Param("drugCode") String drugCode);
}
