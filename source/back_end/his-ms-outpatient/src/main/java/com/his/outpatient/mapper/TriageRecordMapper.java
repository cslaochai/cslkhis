package com.his.outpatient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.outpatient.entity.TriageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门诊分诊记录 Mapper
 */
@Mapper
public interface TriageRecordMapper extends BaseMapper<TriageRecord> {
}
