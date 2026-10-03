package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizRecordQcFlowAction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 病历三级质控流转动作 Mapper（时间线只增不改，BaseMapper 够用）
 */
@Mapper
public interface BizRecordQcFlowActionMapper extends BaseMapper<BizRecordQcFlowAction> {
}
