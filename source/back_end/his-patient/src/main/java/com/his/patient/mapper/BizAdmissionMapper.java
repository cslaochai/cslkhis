package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.InpatientQueryPageDTO;
import com.his.patient.entity.BizAdmission;
import com.his.patient.vo.InpatientDetailVO;
import com.his.patient.vo.InpatientVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 入院登记 Mapper
 */
@Mapper
public interface BizAdmissionMapper extends BaseMapper<BizAdmission> {

    /**
     * 住院列表分页（在院 / 已出院共用，用 admitStatus 区分）
     * <p>{@code scopeDeptIds} 是科室数据权限（M6）的服务端收敛集合，前端不可见：
     * 非空时强制 {@code dept_id IN}，与 {@code q.deptId} 的显式筛选叠加（先经越权校验）。
     */
    @Select("""
            <script>
            SELECT a.admission_id     AS admissionId,
                   a.admission_no     AS admissionNo,
                   a.patient_id       AS patientId,
                   p.patient_no       AS patientNo,
                   p.patient_name     AS patientName,
                   p.gender           AS gender,
                   p.age              AS age,
                   p.medical_insurance_type AS insuranceType,
                   a.dept_id          AS deptId,
                   d.dept_name        AS deptName,
                   a.ward_id          AS wardId,
                   w.ward_name        AS wardName,
                   a.bed_id           AS bedId,
                   b.bed_no           AS bedNo,
                   e.emp_name         AS doctorName,
                   a.admit_time       AS admitTime,
                   a.admit_way        AS admitWay,
                   a.discharge_time   AS dischargeTime,
                   a.admit_status     AS admitStatus,
                   a.diagnosis        AS diagnosis,
                   a.regist_no        AS registNo,
                   o.order_no         AS admissionOrderNo,
                   s.id               AS summaryId,
                   s.summary_status   AS summaryStatus,
                   s.inpatient_days   AS inpatientDays,
                   s.main_diagnosis_name AS mainDiagnosisName,
                   s.total_amount     AS totalAmount
            FROM biz_admission a
                     LEFT JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
                     LEFT JOIN sys_department d ON d.id = a.dept_id AND d.del_flag = 0
                     LEFT JOIN sys_ward w ON w.ward_id = a.ward_id
                     LEFT JOIN sys_bed b ON b.bed_id = a.bed_id AND b.del_flag = 0
                     LEFT JOIN sys_employee e ON e.id = a.admit_doctor_id AND e.del_flag = 0
                     LEFT JOIN biz_admission_order o ON o.id = a.admission_order_id AND o.del_flag = 0
                     LEFT JOIN biz_inpatient_summary s ON s.admission_id = a.admission_id AND s.del_flag = 0
            WHERE a.del_flag = 0
              AND (#{q.admitStatus} IS NULL OR a.admit_status = #{q.admitStatus})
              AND (#{q.deptId} IS NULL OR a.dept_id = #{q.deptId})
              <if test="q.scopeDeptIds != null and q.scopeDeptIds.size() > 0">
                AND a.dept_id IN
                <foreach collection="q.scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
              </if>
              AND (#{q.wardId} IS NULL OR a.ward_id = #{q.wardId})
              AND (#{q.patientName} IS NULL OR #{q.patientName} = ''
                   OR p.patient_name LIKE CONCAT('%', #{q.patientName}, '%')
                   OR a.admission_no LIKE CONCAT('%', #{q.patientName}, '%'))
            ORDER BY a.admit_status DESC, a.admit_time DESC
            </script>
            """)
    IPage<InpatientVO> selectInpatientPage(IPage<InpatientVO> page, @Param("q") InpatientQueryPageDTO query);

    /**
     * 今日入院数（scopeDeptIds 非空时按科室数据权限集合收敛，M6）
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND DATE(admit_time) = CURDATE()
            <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
              AND dept_id IN <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
            </if>
            </script>
            """)
    long countTodayAdmitted(@Param("scopeDeptIds") java.util.List<Long> scopeDeptIds);

    /**
     * 今日出院数（scopeDeptIds 非空时按科室数据权限集合收敛，M6）
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_admission
            WHERE del_flag = 0 AND admit_status = 0 AND DATE(discharge_time) = CURDATE()
            <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
              AND dept_id IN <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
            </if>
            </script>
            """)
    long countTodayDischarged(@Param("scopeDeptIds") java.util.List<Long> scopeDeptIds);

    /**
     * 同一患者当前是否在院（入院重复校验）
     */
    @Select("SELECT COUNT(*) FROM biz_admission WHERE del_flag = 0 AND admit_status = 1 AND patient_id = #{patientId}")
    long countInHospitalByPatient(@Param("patientId") Long patientId);

    /**
     * 该患者 31 日内是否还有其他出院记录（再入院判定，DRG 绩效指标）
     */
    @Select("""
            SELECT COUNT(*)
            FROM biz_admission a
            WHERE a.del_flag = 0
              AND a.patient_id = #{patientId}
              AND a.admission_id <> #{excludeAdmissionId}
              AND a.admit_status = 0
              AND a.discharge_time IS NOT NULL
              AND a.discharge_time >= DATE_SUB(#{dischargeTime}, INTERVAL 31 DAY)
              AND a.discharge_time <= #{dischargeTime}
            """)
    long countReadmitWithin31d(@Param("patientId") Long patientId,
                               @Param("excludeAdmissionId") Long excludeAdmissionId,
                               @Param("dischargeTime") LocalDateTime dischargeTime);

    /**
     * 单条入院的完整展示信息（详情页用，一次 JOIN 取全）
     */
    @Select("""
            SELECT a.admission_id     AS admissionId,
                   a.admission_no     AS admissionNo,
                   a.patient_id       AS patientId,
                   p.patient_no       AS patientNo,
                   p.patient_name     AS patientName,
                   p.gender           AS gender,
                   p.age              AS age,
                   p.id_card          AS idCard,
                   p.medical_insurance_no AS medicalInsuranceNo,
                   p.medical_insurance_type AS insuranceType,
                   a.dept_id          AS deptId,
                   d.dept_name        AS deptName,
                   a.ward_id          AS wardId,
                   w.ward_name        AS wardName,
                   a.bed_id           AS bedId,
                   b.bed_no           AS bedNo,
                   a.admit_doctor_id  AS admitDoctorId,
                   e.emp_name         AS doctorName,
                   a.admit_time       AS admitTime,
                   a.discharge_time   AS dischargeTime,
                   a.admit_way        AS admitWay,
                   a.admit_status     AS admitStatus,
                   a.diagnosis        AS diagnosis,
                   a.visit_id         AS visitId,
                   a.regist_id        AS registId,
                   a.regist_no        AS registNo,
                   a.admission_order_id AS admissionOrderId,
                   o.order_no         AS admissionOrderNo,
                   a.remark           AS remark
            FROM biz_admission a
                     LEFT JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
                     LEFT JOIN sys_department d ON d.id = a.dept_id AND d.del_flag = 0
                     LEFT JOIN sys_ward w ON w.ward_id = a.ward_id
                     LEFT JOIN sys_bed b ON b.bed_id = a.bed_id AND b.del_flag = 0
                     LEFT JOIN sys_employee e ON e.id = a.admit_doctor_id AND e.del_flag = 0
                     LEFT JOIN biz_admission_order o ON o.id = a.admission_order_id AND o.del_flag = 0
            WHERE a.del_flag = 0 AND a.admission_id = #{admissionId}
            """)
    InpatientDetailVO.AdmissionInfo selectAdmissionInfo(@Param("admissionId") Long admissionId);
}
