package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.vo.BizDrugStockLogVO;
import com.his.pharmacy.entity.BizDrugStockLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 药品库存流水Mapper
 * 口径（sql/45 起，sql/154 沿用）：库存的一切增减都必须同事务落一行流水，
 * source_type + source_id + source_no 三元组就是"这笔变动由哪张单据造成"的抓手。
 */
@Mapper
public interface BizDrugStockLogMapper extends BaseMapper<BizDrugStockLog> {

    /** 公共列：库位从批次带出，流水表本身不存（批次是唯一的库位事实来源） */
    String COLUMNS = "l.id, l.create_by, l.create_time, l.update_by, l.update_time, l.del_flag, l.remark, "
            + "l.stock_id, l.drug_id, d.drug_name, l.batch_no, l.change_type, "
            + "l.change_quantity, l.quantity_before, l.quantity_after, "
            + "l.source_type, l.source_id, l.source_no, l.operator_name, s.stock_room ";

    /**
     * 流水分页（联表药品字典带出药名，支持药品名/变动类型过滤）
     */
    @Select("<script>" +
            "SELECT " + COLUMNS +
            "FROM biz_drug_stock_log l " +
            "LEFT JOIN sys_drug d ON l.drug_id = d.id " +
            "LEFT JOIN biz_drug_stock s ON s.id = l.stock_id " +
            "WHERE l.del_flag = 0 " +
            "<if test='drugName != null and drugName != \"\"'> AND d.drug_name LIKE CONCAT('%', #{drugName}, '%') </if> " +
            "<if test='changeType != null'> AND l.change_type = #{changeType} </if> " +
            "ORDER BY l.create_time DESC, l.id DESC" +
            "</script>")
    Page<BizDrugStockLogVO> selectLogPageWithDrug(Page<BizDrugStockLogVO> page,
                                                  @Param("drugName") String drugName,
                                                  @Param("changeType") Integer changeType);

    /**
     * 某张盘点单落下的流水（盘盈 5 / 盘亏 6）
     * <p>盘点单详情靠它把「差异过到哪儿去了」摊开给复核人看：
     * source_type='stocktake' + source_id 就是整单可追溯的抓手。
     */
    @Select("SELECT " + COLUMNS +
            "FROM biz_drug_stock_log l " +
            "LEFT JOIN sys_drug d ON l.drug_id = d.id " +
            "LEFT JOIN biz_drug_stock s ON s.id = l.stock_id " +
            "WHERE l.del_flag = 0 AND l.source_type = 'stocktake' AND l.source_id = #{stocktakeId} " +
            "ORDER BY l.id ASC")
    List<BizDrugStockLogVO> selectByStocktakeId(@Param("stocktakeId") Long stocktakeId);

    /**
     * 某张来源单据落下的全部流水（调拨单、供应商退货单、发药单通用）
     * <p>调拨一张单会出<b>两行</b>（7 为负、8 为正，合计 0），所以这里按单据捞而不是按批次捞，
     * 才能一眼看出"搬出去了还没搬进来"。
     */
    @Select("SELECT " + COLUMNS +
            "FROM biz_drug_stock_log l " +
            "LEFT JOIN sys_drug d ON l.drug_id = d.id " +
            "LEFT JOIN biz_drug_stock s ON s.id = l.stock_id " +
            "WHERE l.del_flag = 0 AND l.source_type = #{sourceType} AND l.source_id = #{sourceId} " +
            "ORDER BY l.id ASC")
    List<BizDrugStockLogVO> selectBySource(@Param("sourceType") String sourceType,
                                           @Param("sourceId") Long sourceId);
}
