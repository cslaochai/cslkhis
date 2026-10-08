package com.his.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.system.vo.PriceItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 跨价表统一查询 Mapper
 */
@Mapper
public interface PriceMapper {

    // 药品字典

    @Select("SELECT 'DRUG' AS itemType, id AS itemId, drug_code AS itemCode, drug_name AS itemName, " +
            "specification AS specification, unit AS unit, manufacturer AS manufacturer, " +
            "price AS price, cost_price AS costPrice, is_medical_insurance AS medicalInsurance, status AS status " +
            "FROM sys_drug " +
            "WHERE del_flag = 0 " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR drug_code LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR drug_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "ORDER BY drug_code, id")
    IPage<PriceItemVO> selectDrugPage(IPage<PriceItemVO> page,
                                      @Param("keyword") String keyword,
                                      @Param("status") Integer status);

    @Select("SELECT 'DRUG' AS itemType, id AS itemId, drug_code AS itemCode, drug_name AS itemName, " +
            "specification AS specification, unit AS unit, manufacturer AS manufacturer, " +
            "price AS price, cost_price AS costPrice, is_medical_insurance AS medicalInsurance, status AS status " +
            "FROM sys_drug WHERE id = #{id} AND del_flag = 0")
    PriceItemVO selectDrugById(@Param("id") Long id);

    @Update("UPDATE sys_drug SET price = #{newPrice}, update_time = NOW() WHERE id = #{id} AND del_flag = 0")
    int updateDrugPrice(@Param("id") Long id, @Param("newPrice") BigDecimal newPrice);

    // 耗材字典

    @Select("SELECT 'CONSUMABLE' AS itemType, id AS itemId, consumable_code AS itemCode, consumable_name AS itemName, " +
            "specification AS specification, unit AS unit, manufacturer AS manufacturer, " +
            "retail_price AS price, status AS status " +
            "FROM sys_consumable " +
            "WHERE del_flag = 0 " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR consumable_code LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR consumable_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "ORDER BY consumable_code, id")
    IPage<PriceItemVO> selectConsumablePage(IPage<PriceItemVO> page,
                                            @Param("keyword") String keyword,
                                            @Param("status") Integer status);

    @Select("SELECT 'CONSUMABLE' AS itemType, id AS itemId, consumable_code AS itemCode, consumable_name AS itemName, " +
            "specification AS specification, unit AS unit, manufacturer AS manufacturer, " +
            "retail_price AS price, status AS status " +
            "FROM sys_consumable WHERE id = #{id} AND del_flag = 0")
    PriceItemVO selectConsumableById(@Param("id") Long id);

    @Update("UPDATE sys_consumable SET retail_price = #{newPrice}, update_time = NOW() WHERE id = #{id} AND del_flag = 0")
    int updateConsumablePrice(@Param("id") Long id, @Param("newPrice") BigDecimal newPrice);

    // 检查项目字典

    @Select("SELECT 'INSPECTION' AS itemType, id AS itemId, item_code AS itemCode, item_name AS itemName, " +
            "price AS price, status AS status " +
            "FROM sys_inspection_item " +
            "WHERE del_flag = 0 " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR item_code LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR item_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "ORDER BY item_code, id")
    IPage<PriceItemVO> selectInspectionPage(IPage<PriceItemVO> page,
                                            @Param("keyword") String keyword,
                                            @Param("status") Integer status);

    @Select("SELECT 'INSPECTION' AS itemType, id AS itemId, item_code AS itemCode, item_name AS itemName, " +
            "price AS price, status AS status " +
            "FROM sys_inspection_item WHERE id = #{id} AND del_flag = 0")
    PriceItemVO selectInspectionById(@Param("id") Long id);

    @Update("UPDATE sys_inspection_item SET price = #{newPrice}, update_time = NOW() WHERE id = #{id} AND del_flag = 0")
    int updateInspectionPrice(@Param("id") Long id, @Param("newPrice") BigDecimal newPrice);

    // 检验项目字典

    @Select("SELECT 'LABORATORY' AS itemType, id AS itemId, item_code AS itemCode, item_name AS itemName, " +
            "unit AS unit, price AS price, status AS status " +
            "FROM sys_laboratory_item " +
            "WHERE del_flag = 0 " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR item_code LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR item_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "ORDER BY item_code, id")
    IPage<PriceItemVO> selectLaboratoryPage(IPage<PriceItemVO> page,
                                            @Param("keyword") String keyword,
                                            @Param("status") Integer status);

    @Select("SELECT 'LABORATORY' AS itemType, id AS itemId, item_code AS itemCode, item_name AS itemName, " +
            "unit AS unit, price AS price, status AS status " +
            "FROM sys_laboratory_item WHERE id = #{id} AND del_flag = 0")
    PriceItemVO selectLaboratoryById(@Param("id") Long id);

    @Update("UPDATE sys_laboratory_item SET price = #{newPrice}, update_time = NOW() WHERE id = #{id} AND del_flag = 0")
    int updateLaboratoryPrice(@Param("id") Long id, @Param("newPrice") BigDecimal newPrice);

    // 治疗项目字典

    @Select("SELECT 'TREATMENT' AS itemType, id AS itemId, item_code AS itemCode, item_name AS itemName, " +
            "price AS price, status AS status " +
            "FROM sys_treatment_item " +
            "WHERE del_flag = 0 " +
            "AND (#{keyword} IS NULL OR #{keyword} = '' OR item_code LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR item_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "ORDER BY item_code, id")
    IPage<PriceItemVO> selectTreatmentPage(IPage<PriceItemVO> page,
                                           @Param("keyword") String keyword,
                                           @Param("status") Integer status);

    @Select("SELECT 'TREATMENT' AS itemType, id AS itemId, item_code AS itemCode, item_name AS itemName, " +
            "price AS price, status AS status " +
            "FROM sys_treatment_item WHERE id = #{id} AND del_flag = 0")
    PriceItemVO selectTreatmentById(@Param("id") Long id);

    @Update("UPDATE sys_treatment_item SET price = #{newPrice}, update_time = NOW() WHERE id = #{id} AND del_flag = 0")
    int updateTreatmentPrice(@Param("id") Long id, @Param("newPrice") BigDecimal newPrice);

    // 汇总统计

    @Select("SELECT " +
            "(SELECT COUNT(*) FROM sys_drug WHERE del_flag = 0) " +
            " + (SELECT COUNT(*) FROM sys_consumable WHERE del_flag = 0) " +
            " + (SELECT COUNT(*) FROM sys_inspection_item WHERE del_flag = 0) " +
            " + (SELECT COUNT(*) FROM sys_laboratory_item WHERE del_flag = 0) " +
            " + (SELECT COUNT(*) FROM sys_treatment_item WHERE del_flag = 0)")
    Long countAllPriceItems();
}
