package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizDischargeDrug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 出院带药单 Mapper。
 */
@Mapper
public interface BizDischargeDrugMapper extends BaseMapper<BizDischargeDrug> {

    /**
     * 入院次归属的患者ID（his-patient 本域入院记录，直接裸查列名已对 information_schema 核对）
     */
    @Select("SELECT patient_id FROM biz_admission WHERE admission_id = #{admissionId} AND del_flag = 0")
    Long selectPatientIdByAdmission(Long admissionId);

    /**
     * 患者快照（患者基本信息列：patient_no / patient_name，跨表裸 SQL 已对列名核对）
     */
    @Select("SELECT id, patient_no AS patientNo, patient_name AS patientName FROM biz_patient "
            + "WHERE id = (SELECT patient_id FROM biz_admission WHERE admission_id = #{admissionId} AND del_flag = 0) "
            + "AND del_flag = 0 LIMIT 1")
    BizDischargeDrug selectPatientSnapshot(Long admissionId);
}
