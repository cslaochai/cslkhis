package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizInvoice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 发票信息Mapper
 */
@Mapper
public interface BizInvoiceMapper extends BaseMapper<BizInvoice> {
}
