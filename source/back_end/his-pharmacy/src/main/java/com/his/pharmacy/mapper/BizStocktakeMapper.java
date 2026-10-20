package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizStocktake;
import com.his.pharmacy.vo.StocktakeVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 药房盘点单 Mapper
 */
@Mapper
public interface BizStocktakeMapper extends BaseMapper<BizStocktake> {

    /**
     * 盘点单分页
     * <p>⚠ ORDER BY 补唯一二级键 id（同秒建单顺序不稳定 → 翻页重复+丢行）
     * <p>⚠ 日期过滤用 DATE(create_time)：{@code create_time <= 'yyyy-MM-dd'} 会漏掉当天全部单据
     */
    @Select("<script>" +
            "SELECT t.id, t.stocktake_no, t.stocktake_title, t.scope_drug_type, t.scope_keyword, t.scope_desc, " +
            "       t.snapshot_time, t.status, t.total_items, t.counted_items, t.diff_items, " +
            "       t.profit_items, t.loss_items, t.diff_quantity, t.diff_amount, " +
            "       t.submit_by, t.submit_time, t.audit_by, t.audit_time, t.audit_remark, " +
            "       t.create_by, t.create_time, t.update_by, t.update_time, t.remark " +
            "FROM biz_stocktake t WHERE t.del_flag = 0 " +
            "<if test='stocktakeNo != null and stocktakeNo != \"\"'> AND t.stocktake_no LIKE CONCAT('%', #{stocktakeNo}, '%') </if> " +
            "<if test='stocktakeTitle != null and stocktakeTitle != \"\"'> AND t.stocktake_title LIKE CONCAT('%', #{stocktakeTitle}, '%') </if> " +
            "<if test='status != null'> AND t.status = #{status} </if> " +
            "<if test='dateStart != null and dateStart != \"\"'> AND DATE(t.create_time) &gt;= #{dateStart} </if> " +
            "<if test='dateEnd != null and dateEnd != \"\"'> AND DATE(t.create_time) &lt;= #{dateEnd} </if> " +
            "ORDER BY t.create_time DESC, t.id DESC" +
            "</script>")
    Page<StocktakeVO> selectStocktakePage(Page<StocktakeVO> page,
                                          @Param("stocktakeNo") String stocktakeNo,
                                          @Param("stocktakeTitle") String stocktakeTitle,
                                          @Param("status") Integer status,
                                          @Param("dateStart") String dateStart,
                                          @Param("dateEnd") String dateEnd);

    @Select("SELECT t.id, t.stocktake_no, t.stocktake_title, t.scope_drug_type, t.scope_keyword, t.scope_desc, " +
            "       t.snapshot_time, t.status, t.total_items, t.counted_items, t.diff_items, " +
            "       t.profit_items, t.loss_items, t.diff_quantity, t.diff_amount, " +
            "       t.submit_by, t.submit_time, t.audit_by, t.audit_time, t.audit_remark, " +
            "       t.create_by, t.create_time, t.update_by, t.update_time, t.remark " +
            "FROM biz_stocktake t WHERE t.del_flag = 0 AND t.id = #{id}")
    StocktakeVO selectStocktakeById(@Param("id") Long id);

    /**
     * 按主键取消盘点单并加行锁
     * <p>录实盘/提交/复核过账都是「判状态 → 改明细 → 回写状态」的多步动作，
     * 不锁行会出现两人同时复核同一张单、差异过两遍账。
     */
    @Select("SELECT * FROM biz_stocktake WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    BizStocktake selectByIdForUpdate(@Param("id") Long id);

    /**
     * ⚠ 物理删：uk_stocktake_no 不含 del_flag，软删的行仍占着单号
     * （AGENTS §3：本表族删除走物理删，见 sql/127 头注）。
     */
    @Delete("DELETE FROM biz_stocktake WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
