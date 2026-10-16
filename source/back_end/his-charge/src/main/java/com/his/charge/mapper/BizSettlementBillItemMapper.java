package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizSettlementBillItem;
import com.his.api.vo.BillFeeTypeSumVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 账单行 Mapper。
 */
@Mapper
public interface BizSettlementBillItemMapper extends BaseMapper<BizSettlementBillItem> {

    @Select("""
            SELECT i.*
            FROM biz_settlement_bill_item i
            WHERE i.del_flag = 0 AND i.bill_id = #{billId}
            ORDER BY i.item_type, i.id
            """)
    List<BizSettlementBillItem> selectByBill(@Param("billId") Long billId);

    /**
     * 这些记账行是否已被别的账单占用（结算前的最后一道闸：
     * 唯一键 uk_bill_fee 只挡同账单重复，跨账单重复捞取要靠这里判）。
     */
    @Select("""
            <script>
            SELECT i.fee_record_id FROM biz_settlement_bill_item i
            JOIN biz_settlement_bill b ON b.id = i.bill_id AND b.del_flag = 0 AND b.bill_status &lt;&gt; 4
            WHERE i.del_flag = 0 AND i.fee_record_id IN
            <foreach item="id" collection="feeIds" open="(" separator="," close=")">#{id}</foreach>
            </script>
            """)
    List<Long> selectOccupiedFeeIds(@Param("feeIds") List<Long> feeIds);

    /**
     * 一批账单各自按费用类别（his_charge_item_type）汇总的行数与金额，给账单列表"费用构成"列用。
     *
     * <p>一次 IN 查询覆盖整页账单（最多 50 条），避免逐单查（N+1）。账单的费行即本表行，
     * 出账时由 {@code bizSettlementBillItemMapper.insert} 落库，是账单内容的权威来源。
     */
    @Select("""
            <script>
            SELECT i.bill_id AS billId, i.item_type AS itemType, COUNT(*) AS count,
                   IFNULL(SUM(i.amount), 0) AS amount
            FROM biz_settlement_bill_item i
            WHERE i.del_flag = 0 AND i.bill_id IN
            <foreach collection='billIds' item='id' open='(' close=')' separator=','>#{id}</foreach>
            GROUP BY i.bill_id, i.item_type
            ORDER BY i.bill_id, i.item_type
            </script>
            """)
    List<BillFeeTypeSumVO> sumByBillIds(@Param("billIds") List<Long> billIds);
}
