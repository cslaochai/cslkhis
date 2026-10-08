package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizDrugStock;
import com.his.pharmacy.vo.BizDrugStockVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 药品库存Mapper
 */
@Mapper
public interface BizDrugStockMapper extends BaseMapper<BizDrugStock> {

    /** 库存列表公共列（新增列时只改这一处，避免三个查询各写一份而漂移） */
    String COLUMNS = "s.id, s.create_by, s.create_time, s.update_by, s.update_time, s.del_flag, s.remark, "
            + "s.drug_id, d.drug_code, d.drug_name, d.specification, d.unit, d.drug_type, d.retail_price, "
            + "s.batch_no, s.production_date, s.expiry_date, s.quantity, s.locked_quantity, "
            + "s.available_quantity, s.cost_price, s.total_amount, s.location, s.stock_room, "
            + "s.supplier, s.supplier_id, s.stock_status ";

    /**
     * 分页查询库存列表（联表药品字典带出药品信息，drugName 模糊搜索）
     */
    @Select("<script>" +
            "SELECT " + COLUMNS +
            "FROM biz_drug_stock s " +
            "LEFT JOIN sys_drug d ON s.drug_id = d.id " +
            "WHERE s.del_flag = 0 " +
            "<if test='drugName != null and drugName != \"\"'> AND (d.drug_name LIKE CONCAT('%', #{drugName}, '%') " +
            "       OR d.drug_code LIKE CONCAT('%', #{drugName}, '%')) </if> " +
            "<if test='stockStatus != null'> AND s.stock_status = #{stockStatus} </if> " +
            "<if test='stockRoom != null'> AND s.stock_room = #{stockRoom} </if> " +
            "ORDER BY s.stock_room ASC, s.expiry_date ASC, s.id ASC" +
            "</script>")
    Page<BizDrugStockVO> selectStockPageWithDrug(Page<BizDrugStockVO> page,
                                                 @Param("drugName") String drugName,
                                                 @Param("stockStatus") Integer stockStatus,
                                                 @Param("stockRoom") Integer stockRoom);

    /**
     * 库存预警列表（不分页，LEFT JOIN 药品字典）
     */
    @Select("<script>" +
            "SELECT " + COLUMNS +
            "FROM biz_drug_stock s " +
            "LEFT JOIN sys_drug d ON s.drug_id = d.id " +
            "WHERE s.del_flag = 0 " +
            "<if test='stockStatus != null'> AND s.stock_status = #{stockStatus} </if> " +
            "ORDER BY s.expiry_date ASC" +
            "</script>")
    List<BizDrugStockVO> selectStockWithDrugInfo(@Param("stockStatus") Integer stockStatus);

    /**
     * 库存详情（LEFT JOIN 药品字典）
     */
    @Select("SELECT " + COLUMNS +
            "FROM biz_drug_stock s " +
            "LEFT JOIN sys_drug d ON s.drug_id = d.id " +
            "WHERE s.del_flag = 0 AND s.id = #{stockId}")
    BizDrugStockVO selectStockDetailWithDrug(@Param("stockId") Long stockId);

    /**
     * 批次候选列表（调拨/退货建单时选批次用）
     *
     * <p>只出 {@code available_quantity > 0} 的批次：锁定量是"已经开方答应给患者的药"，
     * 拿它去调拨或退货等于把患者的药搬走，闸门在 service 层，这里先按可用量筛掉绝大部分误选。
     * <br>{@code onlyWithSupplier} 用于供应商退货：批次没挂供应商档案就不知道该退给谁，直接不出现。
     */
    @Select("<script>" +
            "SELECT " + COLUMNS +
            "FROM biz_drug_stock s " +
            "LEFT JOIN sys_drug d ON s.drug_id = d.id " +
            "WHERE s.del_flag = 0 AND IFNULL(s.available_quantity, 0) &gt; 0 " +
            "<if test='stockRoom != null'> AND s.stock_room = #{stockRoom} </if> " +
            "<if test='onlyWithSupplier'> AND s.supplier_id IS NOT NULL </if> " +
            "<if test='keyword != null and keyword != \"\"'> AND (d.drug_name LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR d.drug_code LIKE CONCAT('%', #{keyword}, '%') OR s.batch_no LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "ORDER BY s.expiry_date ASC, s.id ASC" +
            "</script>")
    List<BizDrugStockVO> selectBatchCandidates(@Param("stockRoom") Integer stockRoom,
                                               @Param("keyword") String keyword,
                                               @Param("onlyWithSupplier") boolean onlyWithSupplier);

    /**
     * 药品字典存在性校验（直查表，不引入 his-system 模块依赖）
     */
    @Select("SELECT COUNT(*) FROM sys_drug WHERE id = #{drugId} AND del_flag = 0")
    long countSysDrugById(@Param("drugId") Long drugId);

    /**
     * 供应商档案存在性校验（直查表，不引入 his-system 模块依赖）
     */
    @Select("SELECT supplier_name FROM sys_supplier WHERE supplier_id = #{supplierId} AND del_flag = 0 AND status = 1")
    String selectSupplierNameById(@Param("supplierId") Long supplierId);

    /**
     * 同药品同批号在同库位下是否已存在库存（新增时防重，走"入库"语义）
     * <p>必须带 stock_room：同批号在药库和药房各一条是常态（整件 + 拆零上架），
     * 不带库位就会把正常的两层库存判成"已存在"。
     */
    @Select("SELECT COUNT(*) FROM biz_drug_stock " +
            "WHERE drug_id = #{drugId} AND batch_no = #{batchNo} AND stock_room = #{stockRoom} AND del_flag = 0")
    long countSameBatch(@Param("drugId") Long drugId, @Param("batchNo") String batchNo,
                        @Param("stockRoom") Integer stockRoom);

    /**
     * FEFO 取可扣减批次（有效期近的先扣），行锁防并发超扣
     */
    @Select("SELECT * FROM biz_drug_stock " +
            "WHERE drug_id = #{drugId} AND stock_room = #{stockRoom} AND del_flag = 0 AND quantity > 0 " +
            "ORDER BY expiry_date ASC, id ASC FOR UPDATE")
    List<BizDrugStock> selectFefoBatchesForUpdate(@Param("drugId") Long drugId,
                                                  @Param("stockRoom") Integer stockRoom);

    /**
     * 该药品某库位全部批次（不过滤数量），FEFO 顺序 + 行锁：开方锁库/撤方解锁用
     */
    @Select("SELECT * FROM biz_drug_stock " +
            "WHERE drug_id = #{drugId} AND stock_room = #{stockRoom} AND del_flag = 0 " +
            "ORDER BY expiry_date ASC, id ASC FOR UPDATE")
    List<BizDrugStock> selectBatchesByDrugForUpdate(@Param("drugId") Long drugId,
                                                    @Param("stockRoom") Integer stockRoom);

    /**
     * 可回库批次（同库位内数量最大的批次优先），行锁防并发
     */
    @Select("SELECT * FROM biz_drug_stock " +
            "WHERE drug_id = #{drugId} AND stock_room = #{stockRoom} AND del_flag = 0 " +
            "ORDER BY quantity DESC, id ASC LIMIT 1 FOR UPDATE")
    BizDrugStock selectRestoreTargetForUpdate(@Param("drugId") Long drugId,
                                              @Param("stockRoom") Integer stockRoom);

    /**
     * 按批次主键取批次并加行锁（盘点过账、调拨发出、退货出库都用它）
     * ⚠ 自定义 SQL 不走 MP 的 @TableLogic，逻辑删除条件必须手写
     */
    @Select("SELECT * FROM biz_drug_stock WHERE id = #{stockId} AND del_flag = 0 FOR UPDATE")
    BizDrugStock selectByIdForUpdate(@Param("stockId") Long stockId);

    /**
     * 按批次主键取批次（不加锁，带药品信息，报错文案与快照用）
     */
    @Select("SELECT s.id, s.create_by, s.create_time, s.update_by, s.update_time, s.del_flag, s.remark, " +
            "       s.drug_id, d.drug_code, d.drug_name, d.specification, d.unit, d.drug_type, d.retail_price, " +
            "       s.batch_no, s.production_date, s.expiry_date, s.quantity, s.locked_quantity, " +
            "       s.available_quantity, s.cost_price, s.total_amount, s.location, s.stock_room, " +
            "       s.supplier, s.supplier_id, s.stock_status " +
            "FROM biz_drug_stock s LEFT JOIN sys_drug d ON d.id = s.drug_id " +
            "WHERE s.del_flag = 0 AND s.id = #{stockId}")
    BizDrugStockVO selectBatchDetail(@Param("stockId") Long stockId);

    /**
     * 按批次主键批量取批次快照（调拨/退货建单时校验并落明细）
     * <p>必须批量：一张调拨单挑十几个批次，逐条查就是 N+1，而建单校验一次就要跑两遍（校验 + 快照）。
     */
    @Select("<script>" +
            "SELECT " + COLUMNS +
            "FROM biz_drug_stock s " +
            "LEFT JOIN sys_drug d ON s.drug_id = d.id " +
            "WHERE s.del_flag = 0 AND s.id IN " +
            "<foreach collection='stockIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>" +
            "</script>")
    List<BizDrugStockVO> selectBatchSnapshots(@Param("stockIds") List<Long> stockIds);

    /**
     * 药品名称（报错文案用）
     */
    @Select("SELECT drug_name FROM sys_drug WHERE id = #{drugId}")
    String selectDrugNameById(@Param("drugId") Long drugId);

    /**
     * 按「药品+批号+库位」取批次，行锁防并发（同批号并发入库/接收不能各算各的）
     * ⚠ 自定义 SQL 不走 MP 的 @TableLogic，逻辑删除条件必须手写
     */
    @Select("SELECT * FROM biz_drug_stock " +
            "WHERE drug_id = #{drugId} AND batch_no = #{batchNo} AND stock_room = #{stockRoom} AND del_flag = 0 " +
            "ORDER BY id ASC LIMIT 1 FOR UPDATE")
    BizDrugStock selectByDrugAndBatchForUpdate(@Param("drugId") Long drugId, @Param("batchNo") String batchNo,
                                               @Param("stockRoom") Integer stockRoom);
}
