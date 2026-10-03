package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizVteStats;
import org.apache.ibatis.annotations.Mapper;

/** VTE 防控月度指标快照 Mapper（本表无 del_flag，覆盖走 upsert） */
@Mapper
public interface BizVteStatsMapper extends BaseMapper<BizVteStats> {
}
