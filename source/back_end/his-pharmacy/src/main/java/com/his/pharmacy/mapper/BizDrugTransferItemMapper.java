package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizDrugTransfer;
import com.his.pharmacy.entity.BizDrugTransferItem;
import com.his.pharmacy.vo.DrugTransferItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 药品调拨明细 Mapper
 */
@Mapper
public interface BizDrugTransferItemMapper extends BaseMapper<BizDrugTransferItem> {

    /**
     * 明细列表（带两侧批次的当前余额：发出后接收前，要看得见「这一批是不是又被别的单据动过」）
     */
    @Select("SELECT i.id, i.transfer_id, i.stock_id, i.in_stock_id, i.drug_id, i.drug_code, i.drug_name, " +
            "       i.specification, i.unit, i.batch_no, i.production_date, i.expiry_date, i.cost_price, " +
            "       i.apply_quantity, i.locked_quantity, i.out_flag, i.in_flag, i.remark, " +
            "       i.apply_quantity * i.cost_price AS amount, " +
            "       IFNULL(fs.quantity, 0) AS from_quantity, IFNULL(ts.quantity, 0) AS to_quantity " +
            "FROM biz_drug_transfer_item i " +
            "LEFT JOIN biz_drug_stock fs ON fs.id = i.stock_id " +
            "LEFT JOIN biz_drug_stock ts ON ts.id = i.in_stock_id " +
            "WHERE i.del_flag = 0 AND i.transfer_id = #{transferId} " +
            "ORDER BY i.drug_code ASC, i.expiry_date ASC, i.id ASC")
    List<DrugTransferItemVO> selectByTransferId(@Param("transferId") Long transferId);

    /**
     * 汇总回写主单的五个口径数
     * <p>out/in 分别按标记位求和：两者不等就是「发出了还没接收」，主单上直接看得见。
     */
    @Select("SELECT COUNT(*) AS total_items, " +
            "       IFNULL(SUM(i.apply_quantity), 0) AS total_quantity, " +
            "       IFNULL(SUM(CASE WHEN i.out_flag = 1 THEN i.apply_quantity ELSE 0 END), 0) AS out_quantity, " +
            "       IFNULL(SUM(CASE WHEN i.in_flag = 1 THEN i.apply_quantity ELSE 0 END), 0) AS in_quantity, " +
            "       IFNULL(SUM(i.apply_quantity * i.cost_price), 0) AS total_amount " +
            "FROM biz_drug_transfer_item i WHERE i.del_flag = 0 AND i.transfer_id = #{transferId}")
    BizDrugTransfer selectSummary(@Param("transferId") Long transferId);

    /** 未发出的批次数（发出确认的闸门） */
    @Select("SELECT COUNT(*) FROM biz_drug_transfer_item " +
            "WHERE del_flag = 0 AND transfer_id = #{transferId} AND out_flag = 0")
    long countNotOut(@Param("transferId") Long transferId);

    /** 未接收的批次数（接收确认的闸门） */
    @Select("SELECT COUNT(*) FROM biz_drug_transfer_item " +
            "WHERE del_flag = 0 AND transfer_id = #{transferId} AND in_flag = 0")
    long countNotIn(@Param("transferId") Long transferId);

    /**
     * 本单是否已有批次发出（有则整单锁定，只能作废重开，不能再改明细）
     * <p>已经动过库存的单据改明细=账面与单据对不上，事后无从核对。
     */
    @Select("SELECT COUNT(*) FROM biz_drug_transfer_item " +
            "WHERE del_flag = 0 AND transfer_id = #{transferId} AND out_flag = 1")
    long countOuted(@Param("transferId") Long transferId);

    @Select("SELECT * FROM biz_drug_transfer_item WHERE id = #{itemId} AND del_flag = 0 FOR UPDATE")
    BizDrugTransferItem selectByIdForUpdate(@Param("itemId") Long itemId);

    /** 接收确认后回写接收方批次ID（明细与真实落位批次连起来，才能反查「这批货现在在哪个库位」） */
    @Update("UPDATE biz_drug_transfer_item SET in_stock_id = #{inStockId}, in_flag = 1, " +
            "       update_by = #{operator}, update_time = #{now} " +
            "WHERE del_flag = 0 AND id = #{itemId}")
    int markIn(@Param("itemId") Long itemId, @Param("inStockId") Long inStockId,
               @Param("operator") String operator, @Param("now") LocalDateTime now);

    @Update("UPDATE biz_drug_transfer_item SET out_flag = 1, update_by = #{operator}, update_time = #{now} " +
            "WHERE del_flag = 0 AND id = #{itemId}")
    int markOut(@Param("itemId") Long itemId,
                @Param("operator") String operator, @Param("now") LocalDateTime now);

    /**
     * ⚠ 物理删：uk_transfer_stock(transfer_id, stock_id) 不含 del_flag，
     * 改明细时若软删，同一批次再选进来必然 Duplicate entry（AGENTS §3）。
     */
    @Delete("DELETE FROM biz_drug_transfer_item WHERE transfer_id = #{transferId}")
    int purgeByTransferId(@Param("transferId") Long transferId);
}
