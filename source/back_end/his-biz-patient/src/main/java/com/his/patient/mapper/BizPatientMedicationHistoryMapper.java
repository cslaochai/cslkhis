package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizPatientMedicationHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者既往用药史Mapper
 */
@Mapper
public interface BizPatientMedicationHistoryMapper extends BaseMapper<BizPatientMedicationHistory> {
}
