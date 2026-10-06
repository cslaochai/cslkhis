package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.YbMappingQueryPageDTO;
import com.his.charge.entity.BizYbMapping;
import com.his.charge.vo.YbMappingListVO;
import com.his.charge.vo.YbMappingStatsVO;
import com.his.charge.vo.YbUnmappedItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 医保目录对照 Mapper。
 *
 * <p>院内目录四张表（药品字典 / 治疗项目字典 / 检验项目字典 /
 * 耗材字典）分属 pharmacy / medicaltech / supplies 模块 ——
 * 按项目铁律跨模块<b>不 import 异模块 Service/Mapper，一律裸 SQL 直查</b>；
 * 列名已对 information_schema.COLUMNS 核对（143 交付时验证）。
 */
@Mapper
public interface BizYbMappingMapper extends BaseMapper<BizYbMapping> {

    /**
     * 解对照物理删（uk_item 不含 del_flag，软删会占键 → 铁律见 BizYbMapping 注释）
     */
    @Delete("DELETE FROM biz_yb_mapping WHERE item_type = #{itemType} AND item_id = #{itemId}")
    int purgeByItem(@Param("itemType") Integer itemType, @Param("itemId") Long itemId);

    // 对照工作台分页（四类型各一条；LEFT JOIN 才能同时出「未对照」行）

    @Select("""
            SELECT m.id AS mapping_id, 1 AS item_type, d.id AS item_id,
                   d.drug_code AS item_code, d.drug_name AS item_name,
                   d.specification AS spec, d.retail_price AS price, d.status AS item_status,
                   m.yb_code, m.yb_name, m.match_type, m.mapped_by, m.mapped_time,
                   c.insurance_level, c.pay_ratio
            FROM sys_drug d
            LEFT JOIN biz_yb_mapping m ON m.item_type = 1 AND m.item_id = d.id
            LEFT JOIN biz_yb_catalog c ON c.id = m.catalog_id
            WHERE d.del_flag = 0
              AND (#{q.keyword} IS NULL OR d.drug_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR d.drug_code LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.yb_code LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.yb_name LIKE CONCAT('%', #{q.keyword}, '%'))
              AND (#{q.mapStatus} IS NULL
                   OR (#{q.mapStatus} = 1 AND m.id IS NOT NULL)
                   OR (#{q.mapStatus} = 0 AND m.id IS NULL))
              AND (#{q.itemStatus} IS NULL OR d.status = #{q.itemStatus})
            ORDER BY IF(m.id IS NULL, 0, 1), d.drug_code
            """)
    IPage<YbMappingListVO> selectDrugPage(IPage<YbMappingListVO> page, @Param("q") YbMappingQueryPageDTO q);

    @Select("""
            SELECT m.id AS mapping_id, 2 AS item_type, t.id AS item_id,
                   t.item_code AS item_code, t.item_name AS item_name,
                   NULL AS spec, t.price AS price, t.status AS item_status,
                   m.yb_code, m.yb_name, m.match_type, m.mapped_by, m.mapped_time,
                   c.insurance_level, c.pay_ratio
            FROM sys_treatment_item t
            LEFT JOIN biz_yb_mapping m ON m.item_type = 2 AND m.item_id = t.id
            LEFT JOIN biz_yb_catalog c ON c.id = m.catalog_id
            WHERE t.del_flag = 0
              AND (#{q.keyword} IS NULL OR t.item_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR t.item_code LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.yb_code LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.yb_name LIKE CONCAT('%', #{q.keyword}, '%'))
              AND (#{q.mapStatus} IS NULL
                   OR (#{q.mapStatus} = 1 AND m.id IS NOT NULL)
                   OR (#{q.mapStatus} = 0 AND m.id IS NULL))
              AND (#{q.itemStatus} IS NULL OR t.status = #{q.itemStatus})
            ORDER BY IF(m.id IS NULL, 0, 1), t.item_code
            """)
    IPage<YbMappingListVO> selectTreatmentPage(IPage<YbMappingListVO> page, @Param("q") YbMappingQueryPageDTO q);

    @Select("""
            SELECT m.id AS mapping_id, 3 AS item_type, l.id AS item_id,
                   l.item_code AS item_code, l.item_name AS item_name,
                   l.specimen_type AS spec, l.price AS price, l.status AS item_status,
                   m.yb_code, m.yb_name, m.match_type, m.mapped_by, m.mapped_time,
                   c.insurance_level, c.pay_ratio
            FROM sys_laboratory_item l
            LEFT JOIN biz_yb_mapping m ON m.item_type = 3 AND m.item_id = l.id
            LEFT JOIN biz_yb_catalog c ON c.id = m.catalog_id
            WHERE l.del_flag = 0
              AND (#{q.keyword} IS NULL OR l.item_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR l.item_code LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.yb_code LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.yb_name LIKE CONCAT('%', #{q.keyword}, '%'))
              AND (#{q.mapStatus} IS NULL
                   OR (#{q.mapStatus} = 1 AND m.id IS NOT NULL)
                   OR (#{q.mapStatus} = 0 AND m.id IS NULL))
              AND (#{q.itemStatus} IS NULL OR l.status = #{q.itemStatus})
            ORDER BY IF(m.id IS NULL, 0, 1), l.item_code
            """)
    IPage<YbMappingListVO> selectLaboratoryPage(IPage<YbMappingListVO> page, @Param("q") YbMappingQueryPageDTO q);

    @Select("""
            SELECT m.id AS mapping_id, 4 AS item_type, s.id AS item_id,
                   s.consumable_code AS item_code, s.consumable_name AS item_name,
                   s.specification AS spec, s.retail_price AS price, s.status AS item_status,
                   m.yb_code, m.yb_name, m.match_type, m.mapped_by, m.mapped_time,
                   c.insurance_level, c.pay_ratio
            FROM sys_consumable s
            LEFT JOIN biz_yb_mapping m ON m.item_type = 4 AND m.item_id = s.id
            LEFT JOIN biz_yb_catalog c ON c.id = m.catalog_id
            WHERE s.del_flag = 0
              AND (#{q.keyword} IS NULL OR s.consumable_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR s.consumable_code LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.yb_code LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.yb_name LIKE CONCAT('%', #{q.keyword}, '%'))
              AND (#{q.mapStatus} IS NULL
                   OR (#{q.mapStatus} = 1 AND m.id IS NOT NULL)
                   OR (#{q.mapStatus} = 0 AND m.id IS NULL))
              AND (#{q.itemStatus} IS NULL OR s.status = #{q.itemStatus})
            ORDER BY IF(m.id IS NULL, 0, 1), s.consumable_code
            """)
    IPage<YbMappingListVO> selectConsumablePage(IPage<YbMappingListVO> page, @Param("q") YbMappingQueryPageDTO q);

    // 统计 / 自动对照

    /**
     * 各类型对照率（total 口径 = del_flag=0 全部，含停用项目 —— 对照率衡量
     * 「目录覆盖面」，停用项目仍会出现在历史结算里，所以不计入排除）。
     */
    @Select("""
            SELECT 1 AS item_type, COUNT(*) AS total,
                   (SELECT COUNT(*) FROM biz_yb_mapping m WHERE m.item_type = 1) AS mapped
            FROM sys_drug d WHERE d.del_flag = 0
            UNION ALL
            SELECT 2, COUNT(*),
                   (SELECT COUNT(*) FROM biz_yb_mapping m WHERE m.item_type = 2)
            FROM sys_treatment_item t WHERE t.del_flag = 0
            UNION ALL
            SELECT 3, COUNT(*),
                   (SELECT COUNT(*) FROM biz_yb_mapping m WHERE m.item_type = 3)
            FROM sys_laboratory_item l WHERE l.del_flag = 0
            UNION ALL
            SELECT 4, COUNT(*),
                   (SELECT COUNT(*) FROM biz_yb_mapping m WHERE m.item_type = 4)
            FROM sys_consumable s WHERE s.del_flag = 0
            """)
    List<YbMappingStatsVO> selectStats();

    /**
     * 未对照院内项目清单（自动对照遍历用；subType=药品 drug_type，其余类型恒 0）
     */
    @Select("""
            SELECT d.id AS item_id, d.drug_code AS item_code, d.drug_name AS item_name,
                   d.drug_type AS sub_type
            FROM sys_drug d
            LEFT JOIN biz_yb_mapping m ON m.item_type = 1 AND m.item_id = d.id
            WHERE d.del_flag = 0 AND m.id IS NULL
            """)
    List<YbUnmappedItemVO> selectUnmappedDrugs();

    @Select("""
            SELECT t.id AS item_id, t.item_code AS item_code, t.item_name AS item_name,
                   0 AS sub_type
            FROM sys_treatment_item t
            LEFT JOIN biz_yb_mapping m ON m.item_type = 2 AND m.item_id = t.id
            WHERE t.del_flag = 0 AND m.id IS NULL
            """)
    List<YbUnmappedItemVO> selectUnmappedTreatments();

    @Select("""
            SELECT l.id AS item_id, l.item_code AS item_code, l.item_name AS item_name,
                   0 AS sub_type
            FROM sys_laboratory_item l
            LEFT JOIN biz_yb_mapping m ON m.item_type = 3 AND m.item_id = l.id
            WHERE l.del_flag = 0 AND m.id IS NULL
            """)
    List<YbUnmappedItemVO> selectUnmappedLaboratories();

    @Select("""
            SELECT s.id AS item_id, s.consumable_code AS item_code, s.consumable_name AS item_name,
                   0 AS sub_type
            FROM sys_consumable s
            LEFT JOIN biz_yb_mapping m ON m.item_type = 4 AND m.item_id = s.id
            WHERE s.del_flag = 0 AND m.id IS NULL
            """)
    List<YbUnmappedItemVO> selectUnmappedConsumables();

    // 院内项目存在性校验（map 前置校验；count 而 select 防大字段）

    @Select("SELECT COUNT(*) FROM sys_drug WHERE del_flag = 0 AND id = #{itemId}")
    long countDrug(@Param("itemId") Long itemId);

    @Select("SELECT COUNT(*) FROM sys_treatment_item WHERE del_flag = 0 AND id = #{itemId}")
    long countTreatment(@Param("itemId") Long itemId);

    @Select("SELECT COUNT(*) FROM sys_laboratory_item WHERE del_flag = 0 AND id = #{itemId}")
    long countLaboratory(@Param("itemId") Long itemId);

    @Select("SELECT COUNT(*) FROM sys_consumable WHERE del_flag = 0 AND id = #{itemId}")
    long countConsumable(@Param("itemId") Long itemId);

    // 院内项目快照（对照落库用；换对照时项目可能已有对照，不能复用未对照清单）

    @Select("""
            SELECT d.id AS item_id, d.drug_code AS item_code, d.drug_name AS item_name,
                   d.drug_type AS sub_type
            FROM sys_drug d WHERE d.del_flag = 0 AND d.id = #{itemId}
            """)
    YbUnmappedItemVO selectDrugSnapshot(@Param("itemId") Long itemId);

    @Select("""
            SELECT t.id AS item_id, t.item_code AS item_code, t.item_name AS item_name,
                   0 AS sub_type
            FROM sys_treatment_item t WHERE t.del_flag = 0 AND t.id = #{itemId}
            """)
    YbUnmappedItemVO selectTreatmentSnapshot(@Param("itemId") Long itemId);

    @Select("""
            SELECT l.id AS item_id, l.item_code AS item_code, l.item_name AS item_name,
                   0 AS sub_type
            FROM sys_laboratory_item l WHERE l.del_flag = 0 AND l.id = #{itemId}
            """)
    YbUnmappedItemVO selectLaboratorySnapshot(@Param("itemId") Long itemId);

    @Select("""
            SELECT s.id AS item_id, s.consumable_code AS item_code, s.consumable_name AS item_name,
                   0 AS sub_type
            FROM sys_consumable s WHERE s.del_flag = 0 AND s.id = #{itemId}
            """)
    YbUnmappedItemVO selectConsumableSnapshot(@Param("itemId") Long itemId);
}
