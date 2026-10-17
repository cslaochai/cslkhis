package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizConsumableStock;
import com.his.pharmacy.vo.BizConsumableStockVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 耗材批次库存Mapper（字典归耗材字典，库存信息一律 JOIN 带出）
 */
@Mapper
public interface BizConsumableStockMapper extends BaseMapper<BizConsumableStock> {

    String STOCK_JOIN_COLS =
            "s.id, s.create_by, s.create_time, s.update_by, s.update_time, s.del_flag, s.remark, " +
                    "       s.consumable_id, c.consumable_code, c.consumable_name, c.category, c.specification, c.unit, " +
                    "       c.retail_price, c.manufacturer, " +
                    "       s.batch_no, s.production_date, s.expiry_date, s.quantity, s.cost_price, s.total_amount, " +
                    "       s.location, s.supplier, s.stock_status ";

    /**
     * 库存分页（耗材名/编码模糊 + 类别 + 状态过滤）
     */
    @Select("<script>" +
            "SELECT " + STOCK_JOIN_COLS +
            "FROM biz_consumable_stock s " +
            "LEFT JOIN sys_consumable c ON s.consumable_id = c.id " +
            "WHERE s.del_flag = 0 " +
            "<if test='keyword != null and keyword != \"\"'> AND (c.consumable_name LIKE CONCAT('%', #{keyword}, '%') OR c.consumable_code LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "<if test='category != null'> AND c.category = #{category} </if> " +
            "<if test='stockStatus != null'> AND s.stock_status = #{stockStatus} </if> " +
            "ORDER BY s.expiry_date ASC, s.id ASC" +
            "</script>")
    Page<BizConsumableStockVO> selectStockPage(Page<BizConsumableStockVO> page,
                                               @Param("keyword") String keyword,
                                               @Param("category") Integer category,
                                               @Param("stockStatus") Integer stockStatus);

    /**
     * 库存详情
     */
    @Select("SELECT " + STOCK_JOIN_COLS +
            "FROM biz_consumable_stock s " +
            "LEFT JOIN sys_consumable c ON s.consumable_id = c.id " +
            "WHERE s.del_flag = 0 AND s.id = #{stockId}")
    BizConsumableStockVO selectStockDetail(@Param("stockId") Long stockId);

    /**
     * 耗材字典存在性校验（直查表，不引 his-system 依赖）
     */
    @Select("SELECT COUNT(*) FROM sys_consumable WHERE id = #{consumableId} AND del_flag = 0")
    long countConsumableById(@Param("consumableId") Long consumableId);

    /**
     * 同耗材同批号防重（新增时走"补货入库"语义）
     */
    @Select("SELECT COUNT(*) FROM biz_consumable_stock WHERE consumable_id = #{consumableId} AND batch_no = #{batchNo} AND del_flag = 0")
    long countSameBatch(@Param("consumableId") Long consumableId, @Param("batchNo") String batchNo);

    /**
     * FEFO 可扣减批次（有效期近的先扣），行锁防并发
     */
    @Select("SELECT * FROM biz_consumable_stock " +
            "WHERE consumable_id = #{consumableId} AND del_flag = 0 AND quantity > 0 " +
            "ORDER BY expiry_date ASC, id ASC FOR UPDATE")
    List<BizConsumableStock> selectFefoBatchesForUpdate(@Param("consumableId") Long consumableId);

    /**
     * 有货批次列表（UDI 扫码候选，FEFO 序，不加锁）
     */
    @Select("SELECT * FROM biz_consumable_stock " +
            "WHERE consumable_id = #{consumableId} AND del_flag = 0 AND quantity >= 1 " +
            "ORDER BY expiry_date ASC, id ASC")
    List<BizConsumableStock> selectInStockBatches(@Param("consumableId") Long consumableId);

    /**
     * 指定批次行锁（高值使用登记：一件一锁，防同一批被并发登记成负数）
     */
    @Select("SELECT * FROM biz_consumable_stock WHERE id = #{stockId} AND del_flag = 0 FOR UPDATE")
    BizConsumableStock selectBatchForUpdate(@Param("stockId") Long stockId);

    /**
     * 耗材名称（报错文案/台账快照用）
     */
    @Select("SELECT consumable_name FROM sys_consumable WHERE id = #{consumableId}")
    String selectConsumableNameById(@Param("consumableId") Long consumableId);

    /**
     * 科室名称（台账快照用，直查科室不引模块依赖）
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptNameById(@Param("deptId") Long deptId);
}
