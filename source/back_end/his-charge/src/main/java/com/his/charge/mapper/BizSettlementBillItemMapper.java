package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizSettlementBillItem;
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
}
