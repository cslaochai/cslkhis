package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizPayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 支付单 Mapper（本模块自有表）。
 */
@Mapper
public interface BizPayOrderMapper extends BaseMapper<BizPayOrder> {

    /**
     * 待支付 → 已支付的条件翻转，返回被改动的行数。
     *
     * <p>必须是「带状态条件的 UPDATE」而不是「先查再改」：渠道会对同一笔交易重试多次通知，
     * 两条通知并发到达时读-改-写会双双看到待支付，于是同一笔钱记两遍收款流水。
     * 条件更新让数据库当闸门：行锁只放过第一个提交者，其余返回 0 行，调用方据此跳过推进。
     */
    @Update("""
            UPDATE biz_pay_order
               SET pay_status = 1, channel = #{channel}, out_trade_no = #{channelTxnNo}, pay_time = #{payTime}, update_time = #{payTime}
             WHERE pay_no = #{payNo} AND pay_status = 0 AND del_flag = 0
            """)
    int markPaidIfPending(@Param("payNo") String payNo,
                          @Param("channel") Integer channel,
                          @Param("channelTxnNo") String channelTxnNo,
                          @Param("payTime") LocalDateTime payTime);

    /**
     * 待支付 → 已关闭的条件翻转（只改还没付的单）。
     *
     * <p>与翻转成已支付同样必须是带状态条件的 UPDATE：收银员点「关闭」和渠道通知到账可能并发，
     * 无条件的关闭会把刚收进来的钱标记成「没这笔收款」，而钱已经在渠道侧了。
     */
    @Update("""
            UPDATE biz_pay_order
               SET pay_status = 2, update_time = #{now}
             WHERE pay_no = #{payNo} AND pay_status = 0 AND del_flag = 0
            """)
    int markClosedIfPending(@Param("payNo") String payNo, @Param("now") LocalDateTime now);

    /**
     * 已支付 → 已退款的条件翻转（只翻已支付的单）。
     *
     * <p>同样不能「先查再改」：退号与退费申请可能同时触发退款，两条链各自读到已支付，
     * 就会把退款时间写成两次、并让「已退」这条状态覆盖掉别的场景刚写进去的备注。
     */
    @Update("""
            UPDATE biz_pay_order
               SET pay_status = 3, refund_time = #{now}, remark = #{reason}, update_time = #{now}
             WHERE pay_no = #{payNo} AND pay_status = 1 AND del_flag = 0
            """)
    int markRefundedIfPaid(@Param("payNo") String payNo,
                           @Param("reason") String reason,
                           @Param("now") LocalDateTime now);

    /**
     * 按支付单号取单（pay_no 唯一）。
     */
    default BizPayOrder selectByPayNo(String payNo) {
        return selectOne(new LambdaQueryWrapper<BizPayOrder>()
                .eq(BizPayOrder::getPayNo, payNo)
                .last("LIMIT 1"));
    }
}
