package com.his.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pay.entity.BizPayOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付单 Mapper（本模块自有表）。
 */
@Mapper
public interface BizPayOrderMapper extends BaseMapper<BizPayOrder> {
}
