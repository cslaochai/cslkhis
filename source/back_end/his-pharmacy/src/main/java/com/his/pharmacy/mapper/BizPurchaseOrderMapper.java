package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizPurchaseOrder;
import com.his.pharmacy.vo.PurchaseOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 采购订单 Mapper
 */
@Mapper
public interface BizPurchaseOrderMapper extends BaseMapper<BizPurchaseOrder> {

    /**
     * 订单分页：LEFT JOIN 供应商名 + 明细条数/数量合计 + 入库单派生列
     *
     * 「是否已入库」是**派生列**：由药品入库单里是否存在已入库（状态3）的单决定。
     * 订单表不存冗余状态 —— 存了就有两个事实源，迟早不一致（订单说已入库、入库单说没入库）。
     *
     * ⚠ 日期过滤先把下单时间取日期再比较，不能拿日期时间直接和日期字符串比：
     *   直接比等价于「小于等于当天零点」，会把当天的单全部漏掉。
     *   消掉时间部分，语义才是"到 9 月 23 日为止（含）"。
     * ⚠ 排序必须补唯一二级键（订单ID）：同秒下单的顺序不稳定 → 翻页重复+丢行，且不报错。
     */
    @Select("<script>" +
            "SELECT o.order_id, o.order_no, o.supplier_id, s.supplier_name, s.supplier_code, " +
            "       o.order_time, o.total_amount, o.approval_status, o.approver_id, o.remark, " +
            "       o.create_by, o.create_time, o.update_by, o.update_time, " +
            "       IFNULL(d.item_count, 0) AS item_count, IFNULL(d.total_quantity, 0) AS total_quantity, " +
            "       ib.last_inbound_no AS inbound_no, " +
            "       IF(IFNULL(ib.done_count, 0) > 0, 1, 0) AS inbound_done " +
            "FROM biz_purchase_order o " +
            "LEFT JOIN sys_supplier s ON s.supplier_id = o.supplier_id AND s.del_flag = 0 " +
            "LEFT JOIN (SELECT order_id, COUNT(*) AS item_count, SUM(quantity) AS total_quantity " +
            "             FROM biz_purchase_order_detail WHERE del_flag = 0 GROUP BY order_id) d " +
            "       ON d.order_id = o.order_id " +
            "LEFT JOIN (SELECT purchase_order_id, " +
            "                  SUM(CASE WHEN inbound_status = 3 THEN 1 ELSE 0 END) AS done_count, " +
            "                  SUBSTRING_INDEX(GROUP_CONCAT(inbound_no ORDER BY id DESC), ',', 1) AS last_inbound_no " +
            "             FROM biz_drug_inbound WHERE del_flag = 0 AND purchase_order_id IS NOT NULL " +
            "            GROUP BY purchase_order_id) ib " +
            "       ON ib.purchase_order_id = o.order_id " +
            "WHERE o.del_flag = 0 " +
            "<if test='orderNo != null and orderNo != \"\"'> AND o.order_no LIKE CONCAT('%', #{orderNo}, '%') </if> " +
            "<if test='supplierId != null'> AND o.supplier_id = #{supplierId} </if> " +
            "<if test='approvalStatus != null'> AND o.approval_status = #{approvalStatus} </if> " +
            "<if test='inboundDone != null'> AND IF(IFNULL(ib.done_count, 0) > 0, 1, 0) = #{inboundDone} </if> " +
            "<if test='dateStart != null and dateStart != \"\"'> AND DATE(o.order_time) &gt;= #{dateStart} </if> " +
            "<if test='dateEnd != null and dateEnd != \"\"'> AND DATE(o.order_time) &lt;= #{dateEnd} </if> " +
            "ORDER BY o.order_time DESC, o.order_id DESC" +
            "</script>")
    Page<PurchaseOrderVO> selectOrderPage(Page<PurchaseOrderVO> page,
                                          @Param("orderNo") String orderNo,
                                          @Param("supplierId") Long supplierId,
                                          @Param("approvalStatus") Integer approvalStatus,
                                          @Param("inboundDone") Boolean inboundDone,
                                          @Param("dateStart") String dateStart,
                                          @Param("dateEnd") String dateEnd);

    /**
     * 单条订单（含供应商名与入库单派生列，明细另查）
     */
    @Select("SELECT o.order_id, o.order_no, o.supplier_id, s.supplier_name, s.supplier_code, " +
            "       o.order_time, o.total_amount, o.approval_status, o.approver_id, o.remark, " +
            "       o.create_by, o.create_time, o.update_by, o.update_time, " +
            "       ib.last_inbound_no AS inbound_no, " +
            "       IF(IFNULL(ib.done_count, 0) > 0, 1, 0) AS inbound_done " +
            "FROM biz_purchase_order o " +
            "LEFT JOIN sys_supplier s ON s.supplier_id = o.supplier_id AND s.del_flag = 0 " +
            "LEFT JOIN (SELECT purchase_order_id, " +
            "                  SUM(CASE WHEN inbound_status = 3 THEN 1 ELSE 0 END) AS done_count, " +
            "                  SUBSTRING_INDEX(GROUP_CONCAT(inbound_no ORDER BY id DESC), ',', 1) AS last_inbound_no " +
            "             FROM biz_drug_inbound WHERE del_flag = 0 AND purchase_order_id IS NOT NULL " +
            "            GROUP BY purchase_order_id) ib " +
            "       ON ib.purchase_order_id = o.order_id " +
            "WHERE o.del_flag = 0 AND o.order_id = #{orderId}")
    PurchaseOrderVO selectOrderById(@Param("orderId") Long orderId);

    /**
     * 按主键取订单并加行锁（审批 / 入库的并发闸门）
     * 入库是「检查状态 → 建批次 → 回写状态」三步，两个请求同时进来会各自读到 inbound_status=0，
     * 结果同一张单入两次库、库存翻倍。必须先把订单行锁住再判状态。
     */
    @Select("SELECT * FROM biz_purchase_order WHERE order_id = #{orderId} AND del_flag = 0 FOR UPDATE")
    BizPurchaseOrder selectByIdForUpdate(@Param("orderId") Long orderId);
}
