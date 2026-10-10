package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.AdmissionOrderQueryPageDTO;
import com.his.patient.entity.BizAdmissionOrder;
import com.his.patient.vo.AdmissionOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住院证 Mapper
 */
@Mapper
public interface BizAdmissionOrderMapper extends BaseMapper<BizAdmissionOrder> {

    /**
     * 住院证分页
     * <p>LEFT JOIN 入院表只为取入院编号（收治后回显），证面信息一律用本表快照，
     * 不做实时 JOIN——证面是"当时写下的"，不能被后来的患者信息变更改写。
     */
    @Select("""
            <script>
            SELECT o.id                  AS id,
                   o.order_no            AS orderNo,
                   o.patient_id          AS patientId,
                   o.patient_no          AS patientNo,
                   o.patient_name        AS patientName,
                   o.gender              AS gender,
                   o.age                 AS age,
                   o.phone               AS phone,
                   o.id_card             AS idCard,
                   o.regist_id           AS registId,
                   o.regist_no           AS registNo,
                   o.visit_id            AS visitId,
                   o.source_dept_id      AS sourceDeptId,
                   o.source_dept_name    AS sourceDeptName,
                   o.source_doctor_id    AS sourceDoctorId,
                   o.source_doctor_name  AS sourceDoctorName,
                   o.apply_dept_id       AS applyDeptId,
                   o.apply_dept_name     AS applyDeptName,
                   o.diagnosis_code      AS diagnosisCode,
                   o.diagnosis_name      AS diagnosisName,
                   o.diagnosis_note      AS diagnosisNote,
                   o.insurance_type      AS insuranceType,
                   o.medical_insurance_no AS medicalInsuranceNo,
                   o.order_status        AS orderStatus,
                   o.expect_admit_time   AS expectAdmitTime,
                   o.order_time          AS orderTime,
                   o.valid_until         AS validUntil,
                   o.admission_id        AS admissionId,
                   a.admission_no        AS admissionNo,
                   o.admit_time          AS admitTime,
                   o.admit_dept_id       AS admitDeptId,
                   d.dept_name           AS admitDeptName,
                   o.cancel_reason       AS cancelReason,
                   o.remark              AS remark
            FROM biz_admission_order o
                     LEFT JOIN biz_admission a ON a.admission_id = o.admission_id AND a.del_flag = 0
                     LEFT JOIN sys_department d ON d.id = o.admit_dept_id AND d.del_flag = 0
            WHERE o.del_flag = 0
              AND (#{q.orderStatus} IS NULL OR o.order_status = #{q.orderStatus})
              AND (#{q.applyDeptId} IS NULL OR o.apply_dept_id = #{q.applyDeptId})
              AND (#{q.sourceDeptId} IS NULL OR o.source_dept_id = #{q.sourceDeptId})
              AND (#{q.registId} IS NULL OR o.regist_id = #{q.registId})
              AND (#{q.patientId} IS NULL OR o.patient_id = #{q.patientId})
              AND (#{q.registNo} IS NULL OR #{q.registNo} = '' OR o.regist_no = #{q.registNo})
              AND (#{q.beginDate} IS NULL OR #{q.beginDate} = ''
                   OR o.order_time >= CONCAT(#{q.beginDate}, ' 00:00:00'))
              AND (#{q.endDate} IS NULL OR #{q.endDate} = ''
                   OR o.order_time <= CONCAT(#{q.endDate}, ' 23:59:59'))
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR o.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR o.order_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR o.regist_no LIKE CONCAT('%', #{q.keyword}, '%'))
              AND (#{q.onlyPending} IS NULL OR #{q.onlyPending} = 0
                   OR (o.order_status = 1 AND (o.valid_until IS NULL OR o.valid_until > NOW())))
              <if test="deptIds != null"> AND o.apply_dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach></if>
            ORDER BY o.order_status ASC, o.order_time DESC
            </script>
            """)
    IPage<AdmissionOrderVO> selectOrderPage(IPage<AdmissionOrderVO> page,
                                            @Param("q") AdmissionOrderQueryPageDTO query,
                                            @Param("deptIds") List<Long> deptIds);

    /**
     * 单证详情（同分页的字段集）
     */
    @Select("""
            SELECT o.id                  AS id,
                   o.order_no            AS orderNo,
                   o.patient_id          AS patientId,
                   o.patient_no          AS patientNo,
                   o.patient_name        AS patientName,
                   o.gender              AS gender,
                   o.age                 AS age,
                   o.phone               AS phone,
                   o.id_card             AS idCard,
                   o.regist_id           AS registId,
                   o.regist_no           AS registNo,
                   o.visit_id            AS visitId,
                   o.source_dept_id      AS sourceDeptId,
                   o.source_dept_name    AS sourceDeptName,
                   o.source_doctor_id    AS sourceDoctorId,
                   o.source_doctor_name  AS sourceDoctorName,
                   o.apply_dept_id       AS applyDeptId,
                   o.apply_dept_name     AS applyDeptName,
                   o.diagnosis_code      AS diagnosisCode,
                   o.diagnosis_name      AS diagnosisName,
                   o.diagnosis_note      AS diagnosisNote,
                   o.insurance_type      AS insuranceType,
                   o.medical_insurance_no AS medicalInsuranceNo,
                   o.order_status        AS orderStatus,
                   o.expect_admit_time   AS expectAdmitTime,
                   o.order_time          AS orderTime,
                   o.valid_until         AS validUntil,
                   o.admission_id        AS admissionId,
                   a.admission_no        AS admissionNo,
                   o.admit_time          AS admitTime,
                   o.admit_dept_id       AS admitDeptId,
                   d.dept_name           AS admitDeptName,
                   o.cancel_reason       AS cancelReason,
                   o.remark              AS remark
            FROM biz_admission_order o
                     LEFT JOIN biz_admission a ON a.admission_id = o.admission_id AND a.del_flag = 0
                     LEFT JOIN sys_department d ON d.id = o.admit_dept_id AND d.del_flag = 0
            WHERE o.del_flag = 0 AND o.id = #{id}
            """)
    AdmissionOrderVO selectOrderDetail(@Param("id") Long id);

    /**
     * 该挂号是否已有「仍然有效」的住院证
     * <p>有效 = 已收治（2），或待收治（1）且未过有效期。
     * <b>已过期的待收治证不算有效</b>——不然患者拿着过期证来，入院处要他重新开证时会被
     * 一句"该挂号已有住院证"堵住，只能去改库。
     *
     * <p>数据库没给 regist_id 建唯一索引：同一挂号作废后允许重新开证。
     * "同一挂号只能有一张有效证"是业务规则，放服务层判，才能给出可读的报错文案。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_admission_order
            WHERE del_flag = 0 AND regist_id = #{registId}
              AND (order_status = 2
                   OR (order_status = 1 AND (valid_until IS NULL OR valid_until > NOW())))
            """)
    long countActiveByRegist(@Param("registId") Long registId);

    /**
     * 待收治且未过期的证数量（住院处首页卡片）
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_admission_order
            WHERE del_flag = 0 AND order_status = 1 AND (valid_until IS NULL OR valid_until > NOW())
              <if test="deptIds != null"> AND apply_dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach></if>
            </script>
            """)
    long countPending(@Param("deptIds") List<Long> deptIds);
}
