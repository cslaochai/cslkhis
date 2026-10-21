package com.his.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.app.entity.BizServiceTicketLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单流转记录。
 */
@Mapper
public interface MiniServiceLogMapper extends BaseMapper<BizServiceTicketLog> {
}
