package com.his.appoint.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.appoint.entity.BizScheduleSlotTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排班模板时间片段Mapper（模板下的半小时段配额）
 */
@Mapper
public interface BizScheduleSlotTemplateMapper extends BaseMapper<BizScheduleSlotTemplate> {
}
