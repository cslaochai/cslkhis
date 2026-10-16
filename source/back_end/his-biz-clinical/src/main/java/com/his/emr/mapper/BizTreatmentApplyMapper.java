package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizTreatmentApply;
import com.his.emr.vo.RegistSnapshotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizTreatmentApplyMapper extends BaseMapper<BizTreatmentApply> {

    /**
     * 挂号快照读（开单时取患者/科室/医生，作为收费明细的来源依据）。
     */
    @Select("SELECT a.id AS registId, a.regist_no AS registNo, a.patient_id AS patientId, "
            + "       a.patient_no AS patientNo, a.patient_name AS patientName, "
            + "       a.dept_id AS deptId, a.dept_name AS deptName, "
            + "       a.doctor_id AS doctorId, a.doctor_name AS doctorName, "
            + "       a.regist_status AS registStatus, a.refund_time AS refundTime "
            + "  FROM biz_appoint_info a WHERE a.id = #{registId} AND a.del_flag = 0")
    RegistSnapshotVO selectRegistSnapshot(@Param("registId") Long registId);
}
