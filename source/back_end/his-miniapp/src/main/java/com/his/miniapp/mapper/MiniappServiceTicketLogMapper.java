package com.his.miniapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.miniapp.entity.BizServiceTicketLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单流转记录。
 */
@Mapper
public interface MiniappServiceTicketLogMapper extends BaseMapper<BizServiceTicketLog> {
}
