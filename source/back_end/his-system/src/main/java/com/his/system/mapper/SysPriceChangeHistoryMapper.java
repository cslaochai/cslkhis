package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysPriceChangeHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格变更历史 Mapper
 */
@Mapper
public interface SysPriceChangeHistoryMapper extends BaseMapper<SysPriceChangeHistory> {
}
