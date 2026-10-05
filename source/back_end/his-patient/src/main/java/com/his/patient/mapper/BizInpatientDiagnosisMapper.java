package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizInpatientDiagnosis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 住院诊断明细 Mapper
 */
@Mapper
public interface BizInpatientDiagnosisMapper extends BaseMapper<BizInpatientDiagnosis> {
}
