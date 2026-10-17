package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper（只读侧；写入由 OperLogInterceptor 旁路落库）。
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
