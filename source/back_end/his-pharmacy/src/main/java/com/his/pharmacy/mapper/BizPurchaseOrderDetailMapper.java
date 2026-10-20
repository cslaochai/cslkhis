package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizPurchaseOrderDetail;
import com.his.pharmacy.vo.PurchaseOrderDetailVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 采购订单明细 Mapper
 */
@Mapper
public interface BizPurchaseOrderDetailMapper extends BaseMapper<BizPurchaseOrderDetail> {

    /**
     * 订单明细（LEFT JOIN 药品字典带出药品名/规格/单位；药品被删则名称可能为空并保留 drug_id）
     */
    @Select("SELECT dt.id, dt.order_id, dt.drug_id, dr.drug_code, dr.drug_name, dr.specification, dr.unit, " +
            "       dt.quantity, dt.unit_price, dt.amount, dt.batch_no, dt.production_date, dt.expiry_date, " +
            "       dt.remark, dt.create_time " +
            "FROM biz_purchase_order_detail dt " +
            "LEFT JOIN sys_drug dr ON dr.id = dt.drug_id " +
            "WHERE dt.del_flag = 0 AND dt.order_id = #{orderId} " +
            "ORDER BY dt.id ASC")
    List<PurchaseOrderDetailVO> selectDetailWithDrug(@Param("orderId") Long orderId);

    /**
     * 订单明细条数
     */
    @Select("SELECT COUNT(*) FROM biz_purchase_order_detail WHERE del_flag = 0 AND order_id = #{orderId}")
    long countByOrder(@Param("orderId") Long orderId);

    /**
     * 订单明细数量合计（入库时校验）
     */
    @Select("SELECT IFNULL(SUM(quantity), 0) FROM biz_purchase_order_detail WHERE del_flag = 0 AND order_id = #{orderId}")
    java.math.BigDecimal sumQuantityByOrder(@Param("orderId") Long orderId);

    /**
     * 物理删除订单明细
     *
     * ⚠ 必须物理删除：明细表 uk_order_drug_batch(order_id, drug_id, batch_no) **不含 del_flag**，
     *   若走逻辑删除，重新录入"同订单+同药品+同批号"会撞唯一键报 Duplicate entry。
     */
    @Delete("DELETE FROM biz_purchase_order_detail WHERE order_id = #{orderId}")
    int deleteByOrderIdPhysically(@Param("orderId") Long orderId);

    /**
     * 逻辑删除单条明细（供 Service 层按 id 删除时使用，内部仍走物理删除语义）
     */
    @Update("UPDATE biz_purchase_order_detail SET del_flag = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);

    /**
     * 药品字典存在性校验（直查表，不引入 his-system 模块依赖）
     */
    @Select("SELECT COUNT(*) FROM sys_drug WHERE id = #{drugId} AND del_flag = 0")
    long countDrugById(@Param("drugId") Long drugId);
}
