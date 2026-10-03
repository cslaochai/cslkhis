package com.his.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.ai.entity.SysAiCallLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 调用审计日志 Mapper
 */
@Mapper
public interface SysAiCallLogMapper extends BaseMapper<SysAiCallLog> {
}
