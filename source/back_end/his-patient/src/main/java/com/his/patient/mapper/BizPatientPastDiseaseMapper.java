package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizPatientPastDisease;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者既往疾病史Mapper
 */
@Mapper
public interface BizPatientPastDiseaseMapper extends BaseMapper<BizPatientPastDisease> {
}
