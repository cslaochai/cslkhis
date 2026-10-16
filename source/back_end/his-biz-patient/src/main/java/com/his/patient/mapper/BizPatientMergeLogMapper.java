package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizPatientMergeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者主索引合并审计 Mapper（P5.1 EMPI）
 */
@Mapper
public interface BizPatientMergeLogMapper extends BaseMapper<BizPatientMergeLog> {

}
