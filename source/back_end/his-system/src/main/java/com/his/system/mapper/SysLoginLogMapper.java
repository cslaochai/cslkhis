package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper（写入由 {@code SysLoginLogService} 旁路落库，读取走日志查看页）。
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {
}
