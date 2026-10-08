package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizCriticalNotice;
import com.his.patient.vo.CriticalNoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 病危重通知 Mapper（sql/161）。
 */
@Mapper
public interface BizCriticalNoticeMapper extends BaseMapper<BizCriticalNotice> {

    /**
     * 台账行：不拖 signer_signature（MEDIUMTEXT 手写图），身份证/电话列表根本不选
     */
    String ROW_COLUMNS = """
            c.id, c.notice_no, c.admission_id, c.patient_id, c.patient_name, c.admission_no,
            c.notice_type, c.consciousness_status, c.clinical_diagnosis, c.notify_time,
            c.doctor_name, c.witness_doctor_name, c.signer_name, c.signer_relation,
            c.acknowledge_time, c.notice_status, c.sign_status, c.issue_time,
            c.print_count, c.last_print_time, c.dept_name, c.ward_name, c.bed_no,
            c.void_reason, c.remark, c.create_time
            """;

    String FILTERS = """
            <if test="keyword != null and keyword != ''">
              AND (c.notice_no LIKE CONCAT('%', #{keyword}, '%')
                OR c.patient_name LIKE CONCAT('%', #{keyword}, '%')
                OR c.admission_no LIKE CONCAT('%', #{keyword}, '%')
                OR c.clinical_diagnosis LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="noticeType != null"> AND c.notice_type = #{noticeType}</if>
            <if test="noticeStatus != null"> AND c.notice_status = #{noticeStatus}</if>
            <if test="deptId != null"> AND c.dept_id = #{deptId}</if>
            <if test="startDateTime != null"> AND c.notify_time &gt;= #{startDateTime}</if>
            <if test="endDateTime != null"> AND c.notify_time &lt;= #{endDateTime}</if>
            """;

    @Select("""
            <script>
            SELECT
            """ + ROW_COLUMNS + """
              FROM biz_critical_notice c
             WHERE c.del_flag = 0
            """ + FILTERS + """
            <if test="deptIds != null">
              AND c.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </if>
             ORDER BY c.notify_time DESC, c.id DESC
            </script>
            """)
    List<CriticalNoticeVO.Row> selectNoticePage(IPage<CriticalNoticeVO.Row> page,
                                                @Param("keyword") String keyword,
                                                @Param("noticeType") Integer noticeType,
                                                @Param("noticeStatus") Integer noticeStatus,
                                                @Param("startDateTime") LocalDateTime startDateTime,
                                                @Param("endDateTime") LocalDateTime endDateTime,
                                                @Param("deptId") Long deptId,
                                                @Param("deptIds") List<Long> deptIds);

    /**
     * 详情＝签收/打印数据源：含手写签名图与关联签名证据（回执注脚），
     * 签收人证件/电话在 SQL 出参层脱敏（本单没有编辑回显需求，明文无回写路径，AGENTS §5）。
     */
    @Select("""
            SELECT c.id, c.notice_no, c.admission_id, c.patient_id, c.patient_name, c.patient_no,
                   c.gender, c.age, c.dept_id, c.dept_name, c.ward_name, c.bed_no, c.admission_no,
                   c.notice_type, c.consciousness_status, c.clinical_diagnosis, c.condition_desc,
                   c.warning_matters, c.doctor_measures, c.notify_time,
                   c.doctor_id, c.doctor_name, c.witness_doctor_id, c.witness_doctor_name,
                   c.signer_name, c.signer_relation,
                   CASE WHEN c.signer_id_card IS NULL OR CHAR_LENGTH(c.signer_id_card) < 8 THEN c.signer_id_card
                        ELSE CONCAT(LEFT(c.signer_id_card, 4), '**********', RIGHT(c.signer_id_card, 4)) END AS signerIdCardMasked,
                   CASE WHEN c.signer_phone IS NULL OR CHAR_LENGTH(c.signer_phone) < 8 THEN c.signer_phone
                        ELSE CONCAT(LEFT(c.signer_phone, 3), '****', RIGHT(c.signer_phone, 4)) END AS signerPhoneMasked,
                   c.signer_signature, c.acknowledge_time, c.notice_status, c.issue_time,
                   c.printer_name, c.print_count, c.last_print_time,
                   c.void_reason, c.void_by, c.void_time,
                   c.sign_status, c.sign_id, c.signed_time, c.remark,
                   s.id            AS signRefId,
                   s.sign_no       AS signNo,
                   s.content_digest AS contentDigest,
                   s.cert_no       AS certNo,
                   s.signed_time   AS sigSignedTime,
                   s.verify_status AS verifyStatus
              FROM biz_critical_notice c
              LEFT JOIN biz_emr_signature s ON s.id = c.sign_id
             WHERE c.del_flag = 0 AND c.id = #{id}
            """)
    CriticalNoticeVO.Detail selectNoticeDetail(@Param("id") Long id);

    /**
     * 开单底稿：住院＋患者一般项目（科室/病区/床位取入院现值）
     */
    @Select("""
            SELECT a.admission_id   AS admissionId,
                   a.admission_no   AS admissionNo,
                   a.patient_id     AS patientId,
                   p.patient_no     AS patientNo,
                   p.patient_name   AS patientName,
                   p.gender         AS gender,
                   TIMESTAMPDIFF(YEAR, p.birth_date, NOW()) AS age,
                   a.dept_id        AS deptId,
                   (SELECT d.dept_name FROM sys_department d WHERE d.id = a.dept_id) AS deptName,
                   (SELECT w.ward_name FROM sys_ward w WHERE w.ward_id = a.ward_id)   AS wardName,
                   (SELECT b.bed_no FROM sys_bed b WHERE b.bed_id = a.bed_id)         AS bedNo,
                   a.diagnosis      AS diagnosis,
                   a.admit_status   AS admitStatus,
                   a.admit_time     AS admitTime
              FROM biz_admission a
              JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
             WHERE a.del_flag = 0 AND a.admission_id = #{admissionId}
            """)
    CriticalNoticeVO.Base selectAdmissionBase(@Param("admissionId") Long admissionId);

    /**
     * 在院患者候选（含每人历史通知张数，医生站横幅数据源）
     */
    @Select("""
            <script>
            SELECT a.admission_id   AS admissionId,
                   a.admission_no   AS admissionNo,
                   a.patient_id     AS patientId,
                   p.patient_name   AS patientName,
                   (SELECT d.dept_name FROM sys_department d WHERE d.id = a.dept_id) AS deptName,
                   (SELECT w.ward_name FROM sys_ward w WHERE w.ward_id = a.ward_id)   AS wardName,
                   (SELECT b.bed_no FROM sys_bed b WHERE b.bed_id = a.bed_id)         AS bedNo,
                   (SELECT COUNT(*) FROM biz_critical_notice n
                     WHERE n.del_flag = 0 AND n.notice_status &lt;&gt; 4
                       AND n.admission_id = a.admission_id) AS noticeCount
              FROM biz_admission a
              JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
             WHERE a.del_flag = 0 AND a.admit_status = 1
            <if test="keyword != null and keyword != ''">
              AND (p.patient_name LIKE CONCAT('%', #{keyword}, '%') OR a.admission_no LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="deptIds != null">
              AND a.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </if>
             ORDER BY a.admit_time DESC
             LIMIT #{limit}
            </script>
            """)
    List<CriticalNoticeVO.Inpatient> selectInpatientCandidates(@Param("keyword") String keyword,
                                                               @Param("deptIds") List<Long> deptIds,
                                                               @Param("limit") int limit);

    /**
     * 医师候选（有医生角色的在职员工，告知/见证医师下拉）
     */
    @Select("""
            SELECT e.id AS employeeId, e.emp_name AS empName, e.dept_name AS deptName
              FROM sys_employee e
             WHERE e.del_flag = 0 AND e.status = 1
               AND EXISTS (SELECT 1 FROM sys_employee_post ep
                            JOIN sys_role r ON r.id = ep.role_id
                           WHERE ep.employee_id = e.id AND r.role_code = '10013')
             ORDER BY e.id
             LIMIT 200
            """)
    List<CriticalNoticeVO.DoctorOption> selectDoctorOptions();

    /**
     * 员工职称码（签名人 signerTitle，电子签名证据留档）
     */
    @Select("SELECT title FROM sys_employee WHERE id = #{employeeId} AND del_flag = 0")
    String selectEmployeeTitle(@Param("employeeId") Long employeeId);

    /**
     * 四状态计数（统计卡）
     */
    @Select("""
            <script>
              SELECT
              COALESCE(SUM(CASE WHEN c.notice_status = 1 THEN 1 ELSE 0 END), 0) AS draftCount,
              COALESCE(SUM(CASE WHEN c.notice_status = 2 THEN 1 ELSE 0 END), 0) AS issuedCount,
              COALESCE(SUM(CASE WHEN c.notice_status = 3 THEN 1 ELSE 0 END), 0) AS ackedCount,
              COALESCE(SUM(CASE WHEN c.notice_status = 4 THEN 1 ELSE 0 END), 0) AS voidCount
              FROM biz_critical_notice c
             WHERE c.del_flag = 0
            <if test="deptIds != null">
              AND c.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </if>
            </script>
            """)
    CriticalNoticeVO.Stats selectStats(@Param("deptIds") List<Long> deptIds);

    /**
     * 该住院当前「已签发待签收」张数（同一在院患者可多次告知，不设唯一闸）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_critical_notice
             WHERE del_flag = 0 AND notice_status = 2 AND admission_id = #{admissionId}
            """)
    int countPendingAckByAdmission(@Param("admissionId") Long admissionId);
}
