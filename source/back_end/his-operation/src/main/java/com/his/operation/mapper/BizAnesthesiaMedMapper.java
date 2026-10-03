package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.BizAnesthesiaMed;
import org.apache.ibatis.annotations.Mapper;

/**
 * 麻醉用药记录 Mapper（只增不改）。
 *
 * <p>没有 UNIQUE 约束：同一时刻同一种药分两次推注是真实情况
 * （负荷剂量后追加），所以这里不按业务键去重 —— 与生命体征表形成对照，
 * 那张表的时间点是真值轴，这张表是流水。
 */
@Mapper
public interface BizAnesthesiaMedMapper extends BaseMapper<BizAnesthesiaMed> {
}
