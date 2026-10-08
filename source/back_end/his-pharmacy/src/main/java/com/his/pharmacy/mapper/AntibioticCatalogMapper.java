package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.vo.AntibioticCatalogVO;
import com.his.pharmacy.vo.AntibioticDrugSelectListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 抗菌药物分级目录（裸 SQL 读写药品字典）。
 */
@Mapper
public interface AntibioticCatalogMapper {

    /**
     * 分级目录分页（keyword 匹配药品名/通用名/编码）。
     * levelFilter：0-只看非抗菌药 1/2/3-只看该级别；null 表示全部。
     */
    @Select("""
            <script>
            SELECT d.id, d.drug_code AS drugCode, d.drug_name AS drugName, d.generic_name AS genericName,
                   d.specification, d.dosage_form AS dosageForm, d.unit, d.category_name AS categoryName,
                   d.antibiotic_level AS antibioticLevel, d.ddd_value AS dddValue, d.ddd_unit_gram AS dddUnitGram
            FROM sys_drug d
            WHERE d.del_flag = 0
              <if test="levelFilter != null">
                AND d.antibiotic_level = #{levelFilter}
              </if>
              <if test="keyword != null and keyword != ''">
                AND (d.drug_name LIKE CONCAT('%', #{keyword}, '%')
                     OR d.generic_name LIKE CONCAT('%', #{keyword}, '%')
                     OR d.drug_code LIKE CONCAT('%', #{keyword}, '%'))
              </if>
            ORDER BY d.antibiotic_level DESC, d.id
            </script>
            """)
    List<AntibioticCatalogVO> selectCatalogPage(Page<?> page,
                                                @Param("keyword") String keyword,
                                                @Param("levelFilter") Integer levelFilter);

    /** 维护分级与 DDD 值（只允许改这三列，药品主数据其余字段归药品维护页） */
    @Update("""
            UPDATE sys_drug
            SET antibiotic_level = #{level},
                ddd_value = #{dddValue},
                ddd_unit_gram = #{dddUnitGram}
            WHERE id = #{id} AND del_flag = 0
            """)
    int updateLevel(@Param("id") Long id,
                    @Param("level") Integer level,
                    @Param("dddValue") java.math.BigDecimal dddValue,
                    @Param("dddUnitGram") java.math.BigDecimal dddUnitGram);

    /** 药品下拉：只取已纳入抗菌药物分级管理的品种（开立授权、点评选药时用） */
    @Select("""
            SELECT d.id, d.drug_name AS drugName, d.specification, d.unit,
                   d.antibiotic_level AS antibioticLevel
            FROM sys_drug d
            WHERE d.del_flag = 0 AND d.antibiotic_level > 0
            ORDER BY d.antibiotic_level, d.id
            """)
    List<AntibioticDrugSelectListVO> selectAntibioticDrugs();

    /** 按 id 取药品分级（开方越权闸用：一次查多 id） */
    @Select("""
            <script>
            SELECT d.id, d.drug_name AS drugName, d.antibiotic_level AS antibioticLevel
            FROM sys_drug d
            WHERE d.del_flag = 0 AND d.antibiotic_level > 0 AND d.id IN
              <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
            </script>
            """)
    List<AntibioticDrugSelectListVO> selectAntibioticByIds(@Param("ids") List<Long> ids);
}
