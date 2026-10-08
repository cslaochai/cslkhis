package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.BizAnesthesiaMed;
import org.apache.ibatis.annotations.Mapper;

/**
 * 麻醉用药记录 Mapper（只增不改）。
 */
@Mapper
public interface BizAnesthesiaMedMapper extends BaseMapper<BizAnesthesiaMed> {
}
