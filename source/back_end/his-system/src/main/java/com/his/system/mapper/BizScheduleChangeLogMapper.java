package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizScheduleChangeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排班变更留痕 Mapper（只写不改不删，故无物理删方法）。
 */
@Mapper
public interface BizScheduleChangeLogMapper extends BaseMapper<BizScheduleChangeLog> {
}
