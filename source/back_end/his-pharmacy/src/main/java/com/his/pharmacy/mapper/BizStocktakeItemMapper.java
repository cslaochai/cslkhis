package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizStocktake;
import com.his.pharmacy.entity.BizStocktakeItem;
import com.his.pharmacy.vo.StocktakeItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 药房盘点明细 Mapper
 */
@Mapper
public interface BizStocktakeItemMapper extends BaseMapper<BizStocktakeItem> {

    /**
     * 账面快照：按范围捞出参与盘点的库存批次（药品信息从字典带出，落库时固化进明细）
     * <p>含 quantity=0 的批次：账面 0、实盘有货正是盘盈，漏掉它等于把这类差异永久藏起来。
     */
    @Select("<script>" +
            "SELECT s.id AS stock_id, s.drug_id, d.drug_code, d.drug_name, d.specification, d.unit, " +
            "       s.batch_no, s.production_date, s.expiry_date, s.location, " +
            "       IFNULL(s.cost_price, 0) AS cost_price, IFNULL(s.locked_quantity, 0) AS locked_quantity, " +
            "       IFNULL(s.quantity, 0) AS book_quantity " +
            "FROM biz_drug_stock s " +
            "LEFT JOIN sys_drug d ON d.id = s.drug_id " +
            "WHERE s.del_flag = 0 " +
            "<if test='drugType != null'> AND d.drug_type = #{drugType} </if> " +
            "<if test='keyword != null and keyword != \"\"'> AND (d.drug_name LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR d.drug_code LIKE CONCAT('%', #{keyword}, '%') OR s.batch_no LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "ORDER BY d.drug_code ASC, s.expiry_date ASC, s.id ASC" +
            "</script>")
    List<StocktakeItemVO> selectSnapshot(@Param("drugType") Integer drugType, @Param("keyword") String keyword);

    /**
     * 明细列表（带当前批次余额：复核时要看得见「快照之后又发过药」）
     */
    @Select("SELECT i.id, i.stocktake_id, i.stock_id, i.drug_id, i.drug_code, i.drug_name, i.specification, " +
            "       i.unit, i.batch_no, i.production_date, i.expiry_date, i.location, i.cost_price, " +
            "       i.locked_quantity, i.book_quantity, i.counted_quantity, i.diff_quantity, i.diff_amount, " +
            "       i.posted, i.remark, IFNULL(s.quantity, 0) AS current_quantity " +
            "FROM biz_stocktake_item i " +
            "LEFT JOIN biz_drug_stock s ON s.id = i.stock_id " +
            "WHERE i.del_flag = 0 AND i.stocktake_id = #{stocktakeId} " +
            "ORDER BY i.drug_code ASC, i.expiry_date ASC, i.id ASC")
    List<StocktakeItemVO> selectByStocktakeId(@Param("stocktakeId") Long stocktakeId);

    /** 汇总回写主单的六个口径数（未录入的批次 diff=0，天然不计入差异） */
    @Select("SELECT COUNT(*) AS total_items, " +
            "       IFNULL(SUM(i.counted_quantity IS NOT NULL), 0) AS counted_items, " +
            "       IFNULL(SUM(i.diff_quantity <> 0), 0) AS diff_items, " +
            "       IFNULL(SUM(i.diff_quantity > 0), 0) AS profit_items, " +
            "       IFNULL(SUM(i.diff_quantity < 0), 0) AS loss_items, " +
            "       IFNULL(SUM(i.diff_quantity), 0) AS diff_quantity, " +
            "       IFNULL(SUM(i.diff_amount), 0) AS diff_amount " +
            "FROM biz_stocktake_item i WHERE i.del_flag = 0 AND i.stocktake_id = #{stocktakeId}")
    BizStocktake selectSummary(@Param("stocktakeId") Long stocktakeId);

    @Select("SELECT COUNT(*) FROM biz_stocktake_item WHERE del_flag = 0 AND stocktake_id = #{stocktakeId}")
    long countByStocktakeId(@Param("stocktakeId") Long stocktakeId);

    /** 未录入实盘数的批次数（提交前的闸门） */
    @Select("SELECT COUNT(*) FROM biz_stocktake_item " +
            "WHERE del_flag = 0 AND stocktake_id = #{stocktakeId} AND counted_quantity IS NULL")
    long countUncounted(@Param("stocktakeId") Long stocktakeId);

    /** 本单是否已有过账明细（复核通过后不允许再改范围） */
    @Select("SELECT COUNT(*) FROM biz_stocktake_item " +
            "WHERE del_flag = 0 AND stocktake_id = #{stocktakeId} AND posted = 1")
    long countPosted(@Param("stocktakeId") Long stocktakeId);

    @Select("SELECT * FROM biz_stocktake_item WHERE id = #{itemId} AND del_flag = 0 FOR UPDATE")
    BizStocktakeItem selectByIdForUpdate(@Param("itemId") Long itemId);

    /**
     * 无差异明细盖章（提交时直接标 2-无差异免过账，复核时就不用再逐条看它们）
     */
    @Update("UPDATE biz_stocktake_item SET posted = 2, update_by = #{operator}, update_time = #{now} " +
            "WHERE del_flag = 0 AND stocktake_id = #{stocktakeId} AND diff_quantity = 0 AND posted = 0")
    int markNoDiff(@Param("stocktakeId") Long stocktakeId,
                   @Param("operator") String operator,
                   @Param("now") LocalDateTime now);

    /**
     * ⚠ 物理删：uk_stocktake_stock(stocktake_id, stock_id) 不含 del_flag，
     * 改范围重快照时若软删，同一批次再进来必然 Duplicate entry（AGENTS §3）。
     */
    @Delete("DELETE FROM biz_stocktake_item WHERE stocktake_id = #{stocktakeId}")
    int purgeByStocktakeId(@Param("stocktakeId") Long stocktakeId);
}
