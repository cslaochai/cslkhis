package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizDeathCertificate;
import com.his.patient.vo.DeathCertificateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 死亡证明 Mapper。
 */
@Mapper
public interface BizDeathCertificateMapper extends BaseMapper<BizDeathCertificate> {

    /**
     * 台账行的显式列：不选 report_payload（TEXT，整页拖报文）也不选身份证（列表不渲染）
     */
    String ROW_COLUMNS = """
            c.id, c.cert_no, c.admission_id, c.discharge_id, c.patient_id, c.patient_name,
            c.gender, c.age, c.death_time, c.death_place, c.death_dept_id, c.death_dept_name,
            c.death_ward_name, c.death_bed_no, c.clinical_diagnosis, c.underlying_icd_code,
            c.underlying_icd_name, c.autopsy_flag, c.physician_name, c.fill_time,
            c.reviewer_name, c.review_time, c.review_opinion, c.issue_time, c.cert_status,
            c.report_status, c.report_deadline, c.report_time, c.report_no, c.report_error,
            c.print_count, c.last_print_time, c.void_reason, c.void_by, c.void_time,
            c.orig_cert_id, c.remark, c.create_by, c.create_time,
            (SELECT o.cert_no FROM biz_death_certificate o WHERE o.id = c.orig_cert_id) AS orig_cert_no
            """;

    String FILTERS = """
            <if test="keyword != null and keyword != ''">
              AND (c.cert_no LIKE CONCAT('%', #{keyword}, '%')
                OR c.patient_name LIKE CONCAT('%', #{keyword}, '%')
                OR c.underlying_icd_code LIKE CONCAT('%', #{keyword}, '%')
                OR c.underlying_icd_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="certStatus != null"> AND c.cert_status = #{certStatus}</if>
            <if test="reportStatus != null"> AND c.report_status = #{reportStatus}</if>
            <if test="deathPlace != null"> AND c.death_place = #{deathPlace}</if>
            <if test="deathDeptId != null"> AND c.death_dept_id = #{deathDeptId}</if>
            <if test="startDateTime != null"> AND c.death_time &gt;= #{startDateTime}</if>
            <if test="endDateTime != null"> AND c.death_time &lt;= #{endDateTime}</if>
            """;

    /**
     * 台账行分页。
     *
     * <p>拼列常量时 {@code SELECT} 后面必须换行：文本块会剥掉行尾空白，写成
     * {@code SELECT """ + ROW_COLUMNS} 编译出来是 {@code SELECTc.id}，运行时报
     * syntax error near 'FROM 死亡医学证明书 c'。
     */
    @Select("""
            <script>
            SELECT
            """ + ROW_COLUMNS + """
              FROM biz_death_certificate c
             WHERE c.del_flag = 0
            """ + FILTERS + """
            <if test="overdue != null and overdue == 1">
              AND c.cert_status = 3 AND c.report_status &lt;&gt; 2
              AND c.report_deadline IS NOT NULL AND c.report_deadline &lt; NOW()
            </if>
             ORDER BY c.death_time DESC, c.id DESC
            </script>
            """)
    List<DeathCertificateVO.Row> selectCertPage(IPage<DeathCertificateVO.Row> page,
                                                @Param("keyword") String keyword,
                                                @Param("certStatus") Integer certStatus,
                                                @Param("reportStatus") Integer reportStatus,
                                                @Param("deathPlace") Integer deathPlace,
                                                @Param("deathDeptId") Long deathDeptId,
                                                @Param("startDateTime") LocalDateTime startDateTime,
                                                @Param("endDateTime") LocalDateTime endDateTime,
                                                @Param("overdue") Integer overdue);

    /**
     * 详情＝编辑回显：一般项目全明文（含身份证号、近亲属电话），另带死因链以外的关联摘要。
     */
    @Select("""
            SELECT c.id, c.cert_no, c.admission_id, c.discharge_id, c.patient_id, c.patient_name,
                   c.gender, c.nation, c.birth_date, c.age, c.id_card, c.occupation, c.marital_status,
                   c.death_time, c.death_place, c.death_dept_id, c.death_dept_name,
                   c.death_ward_name, c.death_bed_no, c.clinical_diagnosis, c.underlying_icd_code,
                   c.underlying_icd_name, c.past_history, c.autopsy_flag, c.autopsy_result,
                   c.relative_name, c.relative_relation, c.relative_phone,
                   c.physician_id, c.physician_name, c.fill_time,
                   c.reviewer_id, c.reviewer_name, c.review_time, c.review_opinion,
                   c.cert_status, c.issue_time, c.print_count, c.last_print_time, c.printer_name,
                   c.void_reason, c.void_by, c.void_time, c.orig_cert_id,
                   c.report_status, c.report_deadline, c.report_time, c.report_no, c.report_error,
                   c.report_payload, c.remark,
                   a.admission_no            AS admissionNo,
                   p.patient_no              AS patientNo,
                   (SELECT o.cert_no FROM biz_death_certificate o WHERE o.id = c.orig_cert_id) AS origCertNo,
                   (SELECT n.cert_no FROM biz_death_certificate n
                     WHERE n.del_flag = 0 AND n.orig_cert_id = c.id ORDER BY n.id DESC LIMIT 1) AS reissueCertNo,
                   (SELECT n.id FROM biz_death_certificate n
                     WHERE n.del_flag = 0 AND n.orig_cert_id = c.id ORDER BY n.id DESC LIMIT 1) AS reissueCertId,
                   (SELECT r.id FROM biz_death_registration r
                     WHERE r.del_flag = 0 AND r.admission_id = c.admission_id
                       AND r.register_status <> 3 ORDER BY r.id DESC LIMIT 1) AS registerId,
                   (SELECT r.register_no FROM biz_death_registration r
                     WHERE r.del_flag = 0 AND r.admission_id = c.admission_id
                       AND r.register_status <> 3 ORDER BY r.id DESC LIMIT 1) AS registerNo,
                   (SELECT r.register_status FROM biz_death_registration r
                     WHERE r.del_flag = 0 AND r.admission_id = c.admission_id
                       AND r.register_status <> 3 ORDER BY r.id DESC LIMIT 1) AS registerStatus,
                   (SELECT COUNT(*) FROM biz_discharge d
                     WHERE d.del_flag = 0 AND d.admission_id = c.admission_id
                       AND d.death_flag = 1) > 0 AS deathDischarged
              FROM biz_death_certificate c
              LEFT JOIN biz_admission a ON a.admission_id = c.admission_id
              LEFT JOIN biz_patient p ON p.id = c.patient_id
             WHERE c.del_flag = 0 AND c.id = #{id}
            """)
    DeathCertificateVO.Detail selectCertDetail(@Param("id") Long id);

    /**
     * 住院＋患者一般项目快照（科室/病区/床位优先取病案首页留档，首页没有再退回入院现值）。
     */
    @Select("""
            SELECT a.admission_id   AS admissionId,
                   a.admission_no   AS admissionNo,
                   a.patient_id     AS patientId,
                   p.patient_no     AS patientNo,
                   p.patient_name   AS patientName,
                   p.gender         AS gender,
                   p.nation         AS nation,
                   p.birth_date     AS birthDate,
                   p.id_card        AS idCard,
                   p.occupation     AS occupation,
                   p.marital_status AS maritalStatus,
                   a.admit_status   AS admitStatus,
                   a.admit_time     AS admitTime,
                   a.diagnosis      AS diagnosis,
                   COALESCE(s.dept_id, a.dept_id)                 AS deptId,
                   COALESCE(s.dept_name,
                            (SELECT d.dept_name FROM sys_department d WHERE d.id = a.dept_id)) AS deptName,
                   COALESCE(s.ward_name,
                            (SELECT w.ward_name FROM sys_ward w WHERE w.ward_id = a.ward_id))   AS wardName,
                   s.bed_no                                       AS bedNo,
                   d.discharge_time                               AS dischargeTime,
                   (SELECT COUNT(*) FROM biz_death_certificate c2
                     WHERE c2.del_flag = 0 AND c2.cert_status <> 4
                       AND c2.admission_id = a.admission_id) > 0  AS hasActiveCert
              FROM biz_admission a
              JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
              LEFT JOIN biz_inpatient_summary s ON s.admission_id = a.admission_id AND s.del_flag = 0
              LEFT JOIN biz_discharge d ON d.del_flag = 0 AND d.death_flag = 1 AND d.admission_id = a.admission_id
             WHERE a.del_flag = 0 AND a.admission_id = #{admissionId}
             ORDER BY d.discharge_id DESC LIMIT 1
            """)
    DeathCertificateVO.PatientSnapshot selectPatientSnapshot(@Param("admissionId") Long admissionId);

    /**
     * 该次住院的「死亡离院」事实（签发前置 + 出院时间必须等于证明死亡时间）。
     */
    @Select("""
            SELECT d.discharge_id   AS dischargeId,
                   d.discharge_no   AS dischargeNo,
                   d.discharge_time AS dischargeTime,
                   d.discharge_way  AS dischargeWay,
                   d.death_flag     AS deathFlag,
                   d.discharge_diagnosis      AS dischargeDiagnosis,
                   d.discharge_diagnosis_code AS dischargeDiagnosisCode
              FROM biz_discharge d
             WHERE d.del_flag = 0 AND d.death_flag = 1 AND d.admission_id = #{admissionId}
             ORDER BY d.discharge_id DESC LIMIT 1
            """)
    DeathCertificateVO.DischargeSnapshot selectDeathDischarge(@Param("admissionId") Long admissionId);

    /**
     * 一次住院的有效证明张数（草稿/已审核/已开具都算占用；excludeId 用于自身修改）。
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_death_certificate
             WHERE del_flag = 0 AND cert_status &lt;&gt; 4 AND admission_id = #{admissionId}
               <if test="excludeId != null"> AND id &lt;&gt; #{excludeId}</if>
            </script>
            """)
    int countActiveByAdmission(@Param("admissionId") Long admissionId, @Param("excludeId") Long excludeId);

    /**
     * 该住院是否已有有效证明（待开证榜点「开证」时给前端提示用）
     */
    @Select("""
            SELECT id FROM biz_death_certificate
             WHERE del_flag = 0 AND cert_status <> 4 AND admission_id = #{admissionId}
             ORDER BY id DESC LIMIT 1
            """)
    Long selectActiveIdByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 待开证榜：已办死亡离院、但没有任何有效证明的住院（欠账榜，同传染病报卡口径）。
     *
     * <p>死亡出院后床位即释放，科室/病区/床位一律读病案首页留档。
     */
    @Select("""
            <script>
            SELECT a.admission_id   AS admissionId,
                   a.admission_no   AS admissionNo,
                   d.patient_id     AS patientId,
                   p.patient_name   AS patientName,
                   p.gender         AS gender,
                   TIMESTAMPDIFF(YEAR, p.birth_date, d.discharge_time) AS age,
                   COALESCE(s.dept_name,
                            (SELECT dn.dept_name FROM sys_department dn WHERE dn.id = a.dept_id)) AS deptName,
                   s.ward_name      AS wardName,
                   s.bed_no         AS bedNo,
                   d.discharge_id   AS dischargeId,
                   d.discharge_no   AS dischargeNo,
                   d.discharge_time AS dischargeTime,
                   d.discharge_diagnosis AS dischargeDiagnosis,
                   0 AS certCount,
                   TIMESTAMPDIFF(DAY, d.discharge_time, NOW()) AS pendingDays,
                   (SELECT r.register_no FROM biz_death_registration r
                     WHERE r.del_flag = 0 AND r.admission_id = d.admission_id
                       AND r.register_status &lt;&gt; 3 ORDER BY r.id DESC LIMIT 1) AS registerNo,
                   (SELECT r.register_status FROM biz_death_registration r
                     WHERE r.del_flag = 0 AND r.admission_id = d.admission_id
                       AND r.register_status &lt;&gt; 3 ORDER BY r.id DESC LIMIT 1) AS registerStatus
              FROM biz_discharge d
              JOIN biz_admission a ON a.admission_id = d.admission_id AND a.del_flag = 0
              JOIN biz_patient p ON p.id = d.patient_id AND p.del_flag = 0
              LEFT JOIN biz_inpatient_summary s ON s.admission_id = d.admission_id AND s.del_flag = 0
             WHERE d.del_flag = 0 AND d.death_flag = 1
               AND NOT EXISTS (SELECT 1 FROM biz_death_certificate c
                                WHERE c.del_flag = 0 AND c.cert_status &lt;&gt; 4
                                  AND c.admission_id = d.admission_id)
              <if test="keyword != null and keyword != ''">
                AND (p.patient_name LIKE CONCAT('%', #{keyword}, '%')
                  OR a.admission_no LIKE CONCAT('%', #{keyword}, '%')
                  OR d.discharge_no LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="startDateTime != null"> AND d.discharge_time &gt;= #{startDateTime}</if>
              <if test="endDateTime != null"> AND d.discharge_time &lt;= #{endDateTime}</if>
             ORDER BY d.discharge_time ASC, d.discharge_id ASC
            </script>
            """)
    List<DeathCertificateVO.PendingRow> selectPendingPage(IPage<DeathCertificateVO.PendingRow> page,
                                                          @Param("keyword") String keyword,
                                                          @Param("startDateTime") LocalDateTime startDateTime,
                                                          @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 逾期催报候选：已开具、未上报成功、已过时限，且今天还没催过（notify_time 按天幂等）。
     *
     * <p>本语句没有 script 包裹，比较符必须写原生小于号（实体转义只在 script 段里才会解码）。
     */
    @Select("""
            SELECT id, cert_no, patient_name, death_time, report_deadline, physician_id, physician_name
              FROM biz_death_certificate
             WHERE del_flag = 0 AND cert_status = 3 AND report_status <> 2
               AND report_deadline IS NOT NULL AND report_deadline < NOW()
               AND (notify_time IS NULL OR notify_time < CURDATE())
             ORDER BY report_deadline ASC
             LIMIT #{limit}
            """)
    List<BizDeathCertificate> selectOverdueForNotify(@Param("limit") int limit);

    /**
     * 统计卡一次取齐（全部是聚合，不返回行数据，避免整页扫表）。
     */
    @Select("""
            SELECT (SELECT COUNT(*) FROM biz_discharge WHERE del_flag = 0 AND death_flag = 1) AS deathDischargeTotal,
                   (SELECT COUNT(*) FROM biz_discharge d WHERE d.del_flag = 0 AND d.death_flag = 1
                     AND NOT EXISTS (SELECT 1 FROM biz_death_certificate c
                                      WHERE c.del_flag = 0 AND c.cert_status <> 4
                                        AND c.admission_id = d.admission_id)) AS noCertCount,
                   (SELECT COUNT(*) FROM biz_death_certificate WHERE del_flag = 0 AND cert_status = 1) AS draftCount,
                   (SELECT COUNT(*) FROM biz_death_certificate WHERE del_flag = 0 AND cert_status = 2) AS auditedCount,
                   (SELECT COUNT(*) FROM biz_death_certificate WHERE del_flag = 0 AND cert_status = 3) AS issuedCount,
                   (SELECT COUNT(*) FROM biz_death_certificate WHERE del_flag = 0 AND cert_status = 4) AS voidCount,
                   (SELECT COUNT(*) FROM biz_death_certificate WHERE del_flag = 0 AND cert_status = 3
                     AND report_status = 1) AS unreportedCount,
                   (SELECT COUNT(*) FROM biz_death_certificate WHERE del_flag = 0 AND report_status = 2) AS reportedCount,
                   (SELECT COUNT(*) FROM biz_death_certificate WHERE del_flag = 0 AND report_status = 3) AS reportFailedCount,
                   (SELECT COUNT(*) FROM biz_death_certificate WHERE del_flag = 0 AND cert_status = 3
                     AND report_status <> 2 AND report_deadline IS NOT NULL
                     AND report_deadline < NOW()) AS overdueCount,
                   (SELECT COUNT(*) FROM biz_discharge d WHERE d.del_flag = 0 AND d.death_flag = 1
                     AND NOT EXISTS (SELECT 1 FROM biz_death_registration r
                                      WHERE r.del_flag = 0 AND r.register_status <> 3
                                        AND r.admission_id = d.admission_id)) AS noRegisterCount,
                   (SELECT COUNT(*) FROM biz_death_registration WHERE del_flag = 0 AND register_status = 2
                     AND death_type <> 1 AND police_flag = 0) AS nonDiseaseUnpolicedCount
            """)
    DeathCertificateVO.Stats selectStats();

    /**
     * 科室名（死亡科室被改选成非当前住院科室时，名字服务端查，不采信前端传来的字符串）。
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    /**
     * 回写病案首页「死亡患者尸检」（国家标准首页项目，以证明为准，两处各填必然漂移）。
     *
     * <p>为什么不走 {@code InpatientService}：出院流程那边已经要调本服务
     * （{@code assertDischargeConsistent}），反向再依赖回去就是循环依赖，Spring 起不来。
     * 这里只写一个列，按项目惯例走裸 SQL，且只在首页存在时生效（0 行＝尚未建首页，不是错误）。
     */
    @Update("""
            UPDATE biz_inpatient_summary SET autopsy_flag = #{autopsyFlag}, update_time = NOW()
             WHERE del_flag = 0 AND admission_id = #{admissionId}
            """)
    int updateSummaryAutopsyFlag(@Param("admissionId") Long admissionId, @Param("autopsyFlag") Integer autopsyFlag);
}
