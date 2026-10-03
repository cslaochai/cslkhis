package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizDrugInbound;
import com.his.pharmacy.vo.DrugInboundVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 药品入库单 Mapper
 */
@Mapper
public interface BizDrugInboundMapper extends BaseMapper<BizDrugInbound> {

    /**
     * 入库单分页（带有效明细条数）
     *
     * ⚠ ORDER BY 必须补唯一二级键 id（同秒生成的单顺序不稳定 → 翻页重复+丢行，且不报错）
     * ⚠ 日期过滤用 DATE(create_time)：`create_time <= '2026-09-23'` 会漏掉当天全部单据
     */
    @Select("<script>" +
            "SELECT i.id, i.inbound_no, i.inbound_type, i.purchase_order_id, i.purchase_order_no, i.supplier, " +
            "       i.total_amount, i.total_quantity, i.inbound_status, i.audit_by, i.audit_time, " +
            "       i.inbound_by, i.inbound_time, i.cancel_by, i.cancel_time, i.cancel_reason, i.remark, " +
            "       i.create_by, i.create_time, i.update_by, i.update_time, " +
            "       IFNULL(d.item_count, 0) AS item_count " +
            "FROM biz_drug_inbound i " +
            "LEFT JOIN (SELECT inbound_id, COUNT(*) AS item_count FROM biz_drug_inbound_detail " +
            "            WHERE del_flag = 0 AND detail_status &lt;&gt; 3 GROUP BY inbound_id) d " +
            "       ON d.inbound_id = i.id " +
            "WHERE i.del_flag = 0 " +
            "<if test='inboundNo != null and inboundNo != \"\"'> AND i.inbound_no LIKE CONCAT('%', #{inboundNo}, '%') </if> " +
            "<if test='inboundType != null'> AND i.inbound_type = #{inboundType} </if> " +
            "<if test='inboundStatus != null'> AND i.inbound_status = #{inboundStatus} </if> " +
            "<if test='purchaseOrderNo != null and purchaseOrderNo != \"\"'> AND i.purchase_order_no LIKE CONCAT('%', #{purchaseOrderNo}, '%') </if> " +
            "<if test='dateStart != null and dateStart != \"\"'> AND DATE(i.create_time) &gt;= #{dateStart} </if> " +
            "<if test='dateEnd != null and dateEnd != \"\"'> AND DATE(i.create_time) &lt;= #{dateEnd} </if> " +
            "ORDER BY i.create_time DESC, i.id DESC" +
            "</script>")
    Page<DrugInboundVO> selectInboundPage(Page<DrugInboundVO> page,
                                          @Param("inboundNo") String inboundNo,
                                          @Param("inboundType") Integer inboundType,
                                          @Param("inboundStatus") Integer inboundStatus,
                                          @Param("purchaseOrderNo") String purchaseOrderNo,
                                          @Param("dateStart") String dateStart,
                                          @Param("dateEnd") String dateEnd);

    /**
     * 入库单详情（明细另查）
     */
    @Select("SELECT i.id, i.inbound_no, i.inbound_type, i.purchase_order_id, i.purchase_order_no, i.supplier, " +
            "       i.total_amount, i.total_quantity, i.inbound_status, i.audit_by, i.audit_time, " +
            "       i.inbound_by, i.inbound_time, i.cancel_by, i.cancel_time, i.cancel_reason, i.remark, " +
            "       i.create_by, i.create_time, i.update_by, i.update_time " +
            "FROM biz_drug_inbound i WHERE i.del_flag = 0 AND i.id = #{id}")
    DrugInboundVO selectInboundById(@Param("id") Long id);

    /**
     * 按主键取入库单并加行锁（审核 / 入库 / 取消的并发闸门）
     * 入库是「判状态 → 建批次 → 回写状态」三步，不锁行会同一张单入两次库、库存翻倍。
     */
    @Select("SELECT * FROM biz_drug_inbound WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    BizDrugInbound selectByIdForUpdate(@Param("id") Long id);

    /**
     * 该采购订单下**未取消**的入库单数（>0 则不允许重复生成：一张采购单对应一张有效入库单）
     */
    @Select("SELECT COUNT(*) FROM biz_drug_inbound " +
            "WHERE del_flag = 0 AND purchase_order_id = #{purchaseOrderId} AND inbound_status <> 4")
    long countActiveByPurchaseOrder(@Param("purchaseOrderId") Long purchaseOrderId);

    /**
     * 该采购订单**已入库**（status=3）的入库单数
     */
    @Select("SELECT COUNT(*) FROM biz_drug_inbound " +
            "WHERE del_flag = 0 AND purchase_order_id = #{purchaseOrderId} AND inbound_status = 3")
    long countStockedByPurchaseOrder(@Param("purchaseOrderId") Long purchaseOrderId);
}
