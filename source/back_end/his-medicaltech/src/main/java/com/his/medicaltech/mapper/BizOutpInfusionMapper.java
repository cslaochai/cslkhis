package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizOutpInfusion;
import com.his.medicaltech.vo.InfusionPatientSnapshotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 门诊输液单 Mapper
 */
@Mapper
public interface BizOutpInfusionMapper extends BaseMapper<BizOutpInfusion> {

    /**
     * 患者主档快照（入座时服务端重查，不信任前端传来的姓名/性别/年龄）。
     */
    @Select("""
            SELECT id           AS patientId,
                   patient_no   AS patientNo,
                   patient_name AS patientName,
                   gender       AS gender,
                   age          AS age
              FROM biz_patient
             WHERE id = #{patientId} AND del_flag = 0
            """)
    InfusionPatientSnapshotVO selectPatientSnapshot(@Param("patientId") Long patientId);
}
