package com.his.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.app.entity.SysServiceTrace;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客服页自助行为埋点（只写，患者侧无读接口；统计走后台报表）。
 */
@Mapper
public interface MiniServiceTraceMapper extends BaseMapper<SysServiceTrace> {
}
