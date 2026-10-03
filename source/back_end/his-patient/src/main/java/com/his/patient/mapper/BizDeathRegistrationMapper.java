package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizDeathRegistration;
import com.his.patient.vo.DeathRegisterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 死亡登记簿 Mapper（关联住院/证明走裸 SQL 只读，不跨域调它们的 Mapper）。
 */
@Mapper
public interface BizDeathRegistrationMapper extends BaseMapper<BizDeathRegistration> {

    String ROW_COLUMNS = """
            r.id, r.register_no, r.admission_id, r.cert_id, r.patient_id, r.patient_name,
            r.death_time, r.death_dept_id, r.death_dept_name, r.death_bed_no, r.death_type,
            r.police_flag, r.police_org, r.police_case_no, r.police_report_time, r.forensic_flag,
            r.body_disposal, r.body_unit, r.body_transport_time, r.relative_name, r.relative_relation,
            r.received_copies, r.receive_time, r.dispute_flag, r.dispute_desc, r.register_status,
            r.registrar_name, r.register_time, r.void_reason, r.void_time, r.remark,
            r.create_by, r.create_time,
            a.admission_no AS admissionNo,
            (SELECT c.cert_no FROM biz_death_certificate c WHERE c.id = r.cert_id) AS certNo
            """;

    /** SELECT 后必须换行：文本块剥行尾空白，同行拼接会编译成 SELECTr.id（见 BizDeathCertificateMapper 同注释） */
    @Select("""
            <script>
            SELECT
            """ + ROW_COLUMNS + """
                   , (SELECT c.cert_status FROM biz_death_certificate c WHERE c.id = r.cert_id) AS certStatus
              FROM biz_death_registration r
              LEFT JOIN biz_admission a ON a.admission_id = r.admission_id
             WHERE r.del_flag = 0
            <if test="keyword != null and keyword != ''">
              AND (r.register_no LIKE CONCAT('%', #{keyword}, '%')
                OR r.patient_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="registerStatus != null"> AND r.register_status = #{registerStatus}</if>
            <if test="deathType != null"> AND r.death_type = #{deathType}</if>
            <if test="policeFlag != null"> AND r.police_flag = #{policeFlag}</if>
            <if test="disputeFlag != null"> AND r.dispute_flag = #{disputeFlag}</if>
            <if test="startDateTime != null"> AND r.death_time &gt;= #{startDateTime}</if>
            <if test="endDateTime != null"> AND r.death_time &lt;= #{endDateTime}</if>
             ORDER BY r.death_time DESC, r.id DESC
            </script>
            """)
    List<DeathRegisterVO.Row> selectRegisterPage(IPage<DeathRegisterVO.Row> page,
                                                @Param("keyword") String keyword,
                                                @Param("registerStatus") Integer registerStatus,
                                                @Param("deathType") Integer deathType,
                                                @Param("policeFlag") Integer policeFlag,
                                                @Param("disputeFlag") Integer disputeFlag,
                                                @Param("startDateTime") LocalDateTime startDateTime,
                                                @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 详情＝编辑回显：办理人电话出明文（整对象回写 upsert，出掩码会洗掉真号）。
     */
    @Select("""
            SELECT
            """ + ROW_COLUMNS + """
                   , r.relative_phone
                   , r.registrar_id
                   , (SELECT c.cert_status FROM biz_death_certificate c WHERE c.id = r.cert_id) AS certStatus
                   , (SELECT d.discharge_time FROM biz_discharge d
                     WHERE d.del_flag = 0 AND d.death_flag = 1 AND d.admission_id = r.admission_id
                     ORDER BY d.discharge_id DESC LIMIT 1) AS dischargeTime
              FROM biz_death_registration r
              LEFT JOIN biz_admission a ON a.admission_id = r.admission_id
             WHERE r.del_flag = 0 AND r.id = #{id}
            """)
    DeathRegisterVO.Detail selectRegisterDetail(@Param("id") Long id);

    /**
     * 一次住院的有效登记条数（草稿/已登记都算占用；作废可重登，故不拦作废行）。
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_death_registration
             WHERE del_flag = 0 AND register_status &lt;&gt; 3 AND admission_id = #{admissionId}
               <if test="excludeId != null"> AND id &lt;&gt; #{excludeId}</if>
            </script>
            """)
    int countActiveByAdmission(@Param("admissionId") Long admissionId, @Param("excludeId") Long excludeId);

    /**
     * 登记底稿：死者姓名/死亡时间/科室床位来自死亡出院与病案首页留档（服务端重查，不信前端），
     * 有有效证明时一并带出证明摘要（登记可以后于签发，也可以先于签发）。
     */
    @Select("""
            SELECT a.admission_id  AS admissionId,
                   a.admission_no  AS admissionNo,
                   a.patient_id    AS patientId,
                   p.patient_name  AS patientName,
                   COALESCE(d.discharge_time, a.discharge_time) AS deathTime,
                   COALESCE(s.dept_id, a.dept_id)               AS deathDeptId,
                   COALESCE(s.dept_name,
                            (SELECT dn.dept_name FROM sys_department dn WHERE dn.id = a.dept_id)) AS deathDeptName,
                   s.bed_no                                     AS deathBedNo,
                   d.discharge_id IS NOT NULL                   AS deathDischarged,
                   c.id                                         AS certId,
                   c.cert_no                                    AS certNo,
                   c.cert_status                                AS certStatus,
                   c.clinical_diagnosis                         AS clinicalDiagnosis,
                   c.underlying_icd_code                        AS underlyingIcdCode,
                   c.underlying_icd_name                        AS underlyingIcdName
              FROM biz_admission a
              JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
              LEFT JOIN biz_inpatient_summary s ON s.admission_id = a.admission_id AND s.del_flag = 0
              LEFT JOIN biz_discharge d ON d.discharge_id = (
                     SELECT d2.discharge_id FROM biz_discharge d2
                      WHERE d2.del_flag = 0 AND d2.death_flag = 1 AND d2.admission_id = a.admission_id
                      ORDER BY d2.discharge_id DESC LIMIT 1)
              LEFT JOIN biz_death_certificate c ON c.id = (
                     SELECT c2.id FROM biz_death_certificate c2
                      WHERE c2.del_flag = 0 AND c2.cert_status <> 4 AND c2.admission_id = a.admission_id
                      ORDER BY c2.id DESC LIMIT 1)
             WHERE a.del_flag = 0 AND a.admission_id = #{admissionId}
            """)
    DeathRegisterVO.Base selectRegisterBase(@Param("admissionId") Long admissionId);

    /** 新建登记时的候选：已办死亡离院的住院（死亡登记的前提是死亡事实已确认） */
    @Select("""
            <script>
            SELECT a.admission_id  AS admissionId,
                   a.admission_no  AS admissionNo,
                   d.patient_id    AS patientId,
                   p.patient_name  AS patientName,
                   d.discharge_time AS deathTime,
                   COALESCE(s.dept_name,
                            (SELECT dn.dept_name FROM sys_department dn WHERE dn.id = a.dept_id)) AS deathDeptName,
                   s.bed_no        AS deathBedNo,
                   1               AS deathDischarged,
                   (SELECT c.id FROM biz_death_certificate c
                     WHERE c.del_flag = 0 AND c.cert_status &lt;&gt; 4 AND c.admission_id = a.admission_id
                     ORDER BY c.id DESC LIMIT 1)       AS certId,
                   (SELECT c.cert_no FROM biz_death_certificate c
                     WHERE c.del_flag = 0 AND c.cert_status &lt;&gt; 4 AND c.admission_id = a.admission_id
                     ORDER BY c.id DESC LIMIT 1)       AS certNo,
                   (SELECT c.cert_status FROM biz_death_certificate c
                     WHERE c.del_flag = 0 AND c.cert_status &lt;&gt; 4 AND c.admission_id = a.admission_id
                     ORDER BY c.id DESC LIMIT 1)       AS certStatus
              FROM biz_discharge d
              JOIN biz_admission a ON a.admission_id = d.admission_id AND a.del_flag = 0
              JOIN biz_patient p ON p.id = d.patient_id AND p.del_flag = 0
              LEFT JOIN biz_inpatient_summary s ON s.admission_id = d.admission_id AND s.del_flag = 0
             WHERE d.del_flag = 0 AND d.death_flag = 1
               AND d.discharge_id = (SELECT MAX(d3.discharge_id) FROM biz_discharge d3
                                      WHERE d3.admission_id = d.admission_id AND d3.del_flag = 0)
              <if test="keyword != null and keyword != ''">
                AND (p.patient_name LIKE CONCAT('%', #{keyword}, '%')
                  OR a.admission_no LIKE CONCAT('%', #{keyword}, '%'))
              </if>
             ORDER BY d.discharge_time DESC
             LIMIT #{limit}
            </script>
            """)
    List<DeathRegisterVO.Base> selectDeathAdmissions(@Param("keyword") String keyword, @Param("limit") int limit);
}
