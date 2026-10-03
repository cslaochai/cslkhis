package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizDischarge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 出院办理 Mapper */
@Mapper
public interface BizDischargeMapper extends BaseMapper<BizDischarge> {

    /** 当天已生成的出院号条数（用于序号） */
    @Select("SELECT COUNT(*) FROM biz_discharge WHERE del_flag = 0 AND discharge_no LIKE CONCAT(#{prefix}, '%')")
    long countByDischargeNoPrefix(@Param("prefix") String prefix);

    /** 该入院的出院记录（正常只有 1 条） */
    @Select("SELECT COUNT(*) FROM biz_discharge WHERE del_flag = 0 AND admission_id = #{admissionId}")
    long countByAdmission(@Param("admissionId") Long admissionId);
}
