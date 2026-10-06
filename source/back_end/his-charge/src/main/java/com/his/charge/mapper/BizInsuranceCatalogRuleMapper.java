package com.his.charge.mapper;

import com.his.charge.entity.BizInsuranceCatalogRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 医保目录报销规则 Mapper。
 */
@Mapper
public interface BizInsuranceCatalogRuleMapper extends BaseMapper<BizInsuranceCatalogRule> {

    /**
     * 按项目编码 + 目录类别 + 就诊类型 + 医保类型查有效规则（优先级升序，取第一条）。
     *
     * <p>生效日期区间：effective_date <= 结算日 AND (expire_date IS NULL OR expire_date >= 结算日)。
     * insurance_type 传 NULL 时匹配通用规则（insurance_type IS NULL）。
     */
    @Select("SELECT * FROM biz_insurance_catalog_rule " +
            " WHERE del_flag = 0 AND status = 1 " +
            "   AND item_code = #{itemCode} " +
            "   AND catalog_type = #{catalogType} " +
            "   AND encounter_type = #{encounterType} " +
            "   AND (insurance_type = #{insuranceType} OR (insurance_type IS NULL AND #{insuranceType} IS NULL)) " +
            "   AND effective_date <= #{settleDate} " +
            "   AND (expire_date IS NULL OR expire_date >= #{settleDate}) " +
            " ORDER BY priority ASC, id ASC " +
            " LIMIT 1")
    BizInsuranceCatalogRule selectEffectiveRule(@Param("itemCode") String itemCode,
                                                @Param("catalogType") Integer catalogType,
                                                @Param("encounterType") Integer encounterType,
                                                @Param("insuranceType") String insuranceType,
                                                @Param("settleDate") java.time.LocalDate settleDate);

    /**
     * 批量查某次结算涉及的所有项目的有效规则（一次查出，避免 N+1）。
     */
    @Select("<script>" +
            "SELECT * FROM biz_insurance_catalog_rule " +
            " WHERE del_flag = 0 AND status = 1 " +
            "   AND encounter_type = #{encounterType} " +
            "   AND (insurance_type = #{insuranceType} OR (insurance_type IS NULL AND #{insuranceType} IS NULL)) " +
            "   AND effective_date &lt;= #{settleDate} " +
            "   AND (expire_date IS NULL OR expire_date &gt;= #{settleDate}) " +
            "   AND item_code IN <foreach collection='itemCodes' item='code' open='(' separator=',' close=')'>" +
            "       #{code}" +
            "   </foreach>" +
            " ORDER BY item_code, priority ASC, id ASC" +
            "</script>")
    List<BizInsuranceCatalogRule> selectBatchRules(@Param("itemCodes") List<String> itemCodes,
                                                   @Param("encounterType") Integer encounterType,
                                                   @Param("insuranceType") String insuranceType,
                                                   @Param("settleDate") java.time.LocalDate settleDate);
}
