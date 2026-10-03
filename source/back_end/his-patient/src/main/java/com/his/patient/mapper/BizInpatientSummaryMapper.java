package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizInpatientSummary;
import org.apache.ibatis.annotations.Mapper;

/** 住院病案首页 Mapper */
@Mapper
public interface BizInpatientSummaryMapper extends BaseMapper<BizInpatientSummary> {
}
