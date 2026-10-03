package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizPatientSurgeryHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者手术外伤史Mapper
 */
@Mapper
public interface BizPatientSurgeryHistoryMapper extends BaseMapper<BizPatientSurgeryHistory> {
}
