package com.his.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.report.entity.BizStatReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 病案统计上报台账 Mapper（CRUD；列表查询在 Service 侧排除 payload 大字段）。
 */
@Mapper
public interface BizStatReportMapper extends BaseMapper<BizStatReport> {
}
