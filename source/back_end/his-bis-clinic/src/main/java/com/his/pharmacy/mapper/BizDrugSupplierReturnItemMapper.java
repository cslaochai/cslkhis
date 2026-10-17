package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizDrugSupplierReturn;
import com.his.pharmacy.entity.BizDrugSupplierReturnItem;
import com.his.pharmacy.vo.SupplierReturnItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 药品供应商退货明细 Mapper
 */
@Mapper
public interface BizDrugSupplierReturnItemMapper extends BaseMapper<BizDrugSupplierReturnItem> {

    /** 明细列表（带批次当前余额：退货后还剩多少在架，一眼看出是不是整批退完） */
    @Select("SELECT i.id, i.return_id, i.stock_id, i.drug_id, i.drug_code, i.drug_name, i.specification, " +
            "       i.unit, i.batch_no, i.expiry_date, i.stock_room, i.supplier_id, i.cost_price, " +
            "       i.quantity, i.amount, i.remark, IFNULL(s.quantity, 0) AS current_quantity " +
            "FROM biz_drug_supplier_return_item i " +
            "LEFT JOIN biz_drug_stock s ON s.id = i.stock_id " +
            "WHERE i.del_flag = 0 AND i.return_id = #{returnId} " +
            "ORDER BY i.drug_code ASC, i.expiry_date ASC, i.id ASC")
    List<SupplierReturnItemVO> selectByReturnId(@Param("returnId") Long returnId);

    /** 汇总回写主单（金额直接取明细上算好的 amount，不再乘一遍，避免两处口径漂移） */
    @Select("SELECT COUNT(*) AS total_items, IFNULL(SUM(i.quantity), 0) AS total_quantity, " +
            "       IFNULL(SUM(i.amount), 0) AS total_amount " +
            "FROM biz_drug_supplier_return_item i WHERE i.del_flag = 0 AND i.return_id = #{returnId}")
    BizDrugSupplierReturn selectSummary(@Param("returnId") Long returnId);

    /**
     * ⚠ 物理删：uk_sreturn_stock(return_id, stock_id) 不含 del_flag，
     * 改明细时若软删，同一批次再选进来必然 Duplicate entry（AGENTS §3）。
     */
    @Delete("DELETE FROM biz_drug_supplier_return_item WHERE return_id = #{returnId}")
    int purgeByReturnId(@Param("returnId") Long returnId);
}
