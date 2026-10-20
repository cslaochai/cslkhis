package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizPayChannelBill;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 支付渠道对账流水 Mapper
 */
@Mapper
public interface BizPayChannelBillMapper extends BaseMapper<BizPayChannelBill> {

    /**
     * 物理删除指定渠道+日期的账单（唯一键不含 del_flag，对账导入前必须清旧）
     */
    @Delete("DELETE FROM biz_pay_channel_bill WHERE channel = #{channel} AND bill_date = #{billDate}")
    void purgeByChannelAndDate(@Param("channel") Integer channel, @Param("billDate") LocalDate billDate);
}
