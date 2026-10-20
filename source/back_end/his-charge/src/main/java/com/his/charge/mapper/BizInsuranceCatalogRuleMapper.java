package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizInsuranceCatalogRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 医保目录报销规则 Mapper。
 *
 * <p>只保留 {@link #selectBatchRules} 一个查询方法：结算一次要算整单所有行，
 * 单条查（{@code selectEffectiveRule}）曾存在但零调用 —— 留着只会让人以为两条路径语义一致，
 * 而它们对 insurance_type 的理解恰恰是两种。以后要单条查，从批量方法里过滤，别再加一个。
 */
@Mapper
public interface BizInsuranceCatalogRuleMapper extends BaseMapper<BizInsuranceCatalogRule> {

    /**
     * 批量查某次结算涉及的所有项目的有效规则（一次查出，避免 N+1）。
     *
     * <p><b>catalog_type 不在这里过滤</b>：同一项目可能同时有甲类和乙类规则，
     * 都要捞回来，由调用方按「项目编码 + 费别」成对取用；在 SQL 里一刀切会砍掉调用方要的那一条。
     * 排序把 catalog_type 放在最前，调用方顺序遍历即可成对命中。
     *
     * <p><b>insurance_type 是「精确优先、通用兜底」，不是「只认精确」</b>：同一项目同一目录类别下，
     * 居民与职工待遇不同，应各配一条带险种的规则；没配到才回落到通用规则（insurance_type IS NULL）。
     * 写成 {@code insurance_type = #{t} OR (insurance_type IS NULL AND #{t} IS NULL)} 是反的：
     * 传了险种就再也匹配不到通用规则，传 null 反而只能匹配通用规则 —— 结果只要患者有医保号，
     * 全表通用规则一条都不命中，统筹全部回落到 byItemType 硬推定。
     *
     * <p>生效日期区间：effective_date <= 结算日 AND (expire_date IS NULL OR expire_date >= 结算日)。
     */
    @Select("<script>" +
            "SELECT * FROM biz_yb_catalog_rule " +
            " WHERE del_flag = 0 AND status = 1 " +
            "   AND encounter_type = #{encounterType} " +
            "   AND (insurance_type IS NULL OR insurance_type = #{insuranceType}) " +
            "   AND effective_date &lt;= #{settleDate} " +
            "   AND (expire_date IS NULL OR expire_date &gt;= #{settleDate}) " +
            "   AND item_code IN <foreach collection='itemCodes' item='code' open='(' separator=',' close=')'>" +
            "       #{code}" +
            "   </foreach>" +
            " ORDER BY item_code, catalog_type, (insurance_type IS NULL) ASC, priority ASC, id ASC" +
            "</script>")
    List<BizInsuranceCatalogRule> selectBatchRules(@Param("itemCodes") List<String> itemCodes,
                                                   @Param("encounterType") Integer encounterType,
                                                   @Param("insuranceType") String insuranceType,
                                                   @Param("settleDate") java.time.LocalDate settleDate);
}
