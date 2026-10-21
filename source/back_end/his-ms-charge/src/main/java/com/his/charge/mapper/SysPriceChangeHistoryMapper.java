package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.SysPriceChangeHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格变更历史 Mapper
 */
@Mapper
public interface SysPriceChangeHistoryMapper extends BaseMapper<SysPriceChangeHistory> {
}
