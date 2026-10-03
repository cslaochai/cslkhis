package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizPatientAllergy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 患者过敏史Mapper
 */
@Mapper
public interface BizPatientAllergyMapper extends BaseMapper<BizPatientAllergy> {
}
