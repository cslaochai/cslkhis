package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizReferral;
import com.his.patient.vo.DeptSnapshotVO;
import com.his.patient.vo.ReferralPatientSnapshotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 双向转诊 Mapper。
 */
@Mapper
public interface BizReferralMapper extends BaseMapper<BizReferral> {

    /**
     * 全量科室名映射（科室数量少，一次性取全，service 里组 Map 回填 VO）
     */
    @Select("SELECT id, dept_name AS deptName FROM sys_department WHERE del_flag = 0")
    List<DeptSnapshotVO> selectDeptMap();

    /**
     * 患者/入院快照（本域表，列名已对 information_schema 核对）：
     * patient 从患者基本信息取号/姓名/电话；admissionId 非空时再带入院号与诊断。
     */
    @Select("SELECT p.patient_no AS patientNo, p.patient_name AS patientName, p.phone AS phone, "
            + "a.admission_no AS admissionNo, a.diagnosis AS diagnosis "
            + "FROM biz_patient p "
            + "LEFT JOIN biz_admission a ON a.admission_id = #{admissionId} AND a.del_flag = 0 "
            + "WHERE p.id = #{patientId} AND p.del_flag = 0 LIMIT 1")
    ReferralPatientSnapshotVO selectPatientSnapshot(@Param("patientId") Long patientId,
                                                     @Param("admissionId") Long admissionId);
}
