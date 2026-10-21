package com.his.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.platform.entity.SysWard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 病区主数据 Mapper（排班侧只读取快照，不写）。
 */
@Mapper
public interface SysWardMapper extends BaseMapper<SysWard> {
}
