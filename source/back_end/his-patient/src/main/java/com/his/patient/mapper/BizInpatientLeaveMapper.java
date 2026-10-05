package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizInpatientLeave;
import com.his.patient.vo.InpatientLeaveVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院请假单 Mapper（sql/162）。
 *
 * <p>入院记录 / 患者基本信息 / 电子签名证据归其他子域，按项目规范不跨域调
 * 它们的 Mapper，这里走裸 SQL 只读（JOIN 侧手工 AND del_flag=0，@TableLogic 不覆盖自定义 SQL）。
 * 列名已对 information_schema（入院记录主键是 admission_id）。
 */
@Mapper
public interface BizInpatientLeaveMapper extends BaseMapper<BizInpatientLeave> {

    /**
     * 台账行：不拖 confirm_signature（MEDIUMTEXT 手写图），电话列表根本不选
     */
    String ROW_COLUMNS = """
            l.id, l.leave_no, l.admission_id, l.patient_id, l.patient_name, l.admission_no,
            l.dept_name, l.ward_name, l.bed_no, l.leave_type, l.reason, l.destination,
            l.companion_name, l.companion_relation,
            DATE_FORMAT(l.expected_leave_time, '%Y-%m-%d %H:%i:%s') AS expectedLeaveTime,
            DATE_FORMAT(l.expected_return_time, '%Y-%m-%d %H:%i:%s') AS expectedReturnTime,
            DATE_FORMAT(l.apply_time, '%Y-%m-%d %H:%i:%s') AS applyTime,
            l.apply_by, l.doctor_name, l.leave_status, l.overdue_contact_result, l.report_to,
            l.print_count, l.sign_status, l.remark
            """;

    String FILTERS = """
            <if test="keyword != null and keyword != ''">
              AND (l.leave_no LIKE CONCAT('%', #{keyword}, '%')
                OR l.patient_name LIKE CONCAT('%', #{keyword}, '%')
                OR l.admission_no LIKE CONCAT('%', #{keyword}, '%')
                OR l.destination LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="leaveType != null"> AND l.leave_type = #{leaveType}</if>
            <if test="leaveStatus != null"> AND l.leave_status = #{leaveStatus}</if>
            <if test="deptId != null"> AND l.dept_id = #{deptId}</if>
            <if test="startDateTime != null"> AND l.apply_time &gt;= #{startDateTime}</if>
            <if test="endDateTime != null"> AND l.apply_time &lt;= #{endDateTime}</if>
            """;

    @Select("""
            <script>
            SELECT
            """ + ROW_COLUMNS + """
              FROM biz_inpatient_leave l
             WHERE l.del_flag = 0
            """ + FILTERS + """
            <if test="overdueOnly != null and overdueOnly">
              AND l.leave_status = 3 AND l.expected_return_time &lt; NOW()
            </if>
            <if test="deptIds != null">
              AND l.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </if>
             ORDER BY l.apply_time DESC, l.id DESC
            </script>
            """)
    List<InpatientLeaveVO.Row> selectLeavePage(IPage<InpatientLeaveVO.Row> page,
                                               @Param("keyword") String keyword,
                                               @Param("leaveType") Integer leaveType,
                                               @Param("leaveStatus") Integer leaveStatus,
                                               @Param("overdueOnly") Boolean overdueOnly,
                                               @Param("startDateTime") LocalDateTime startDateTime,
                                               @Param("endDateTime") LocalDateTime endDateTime,
                                               @Param("deptId") Long deptId,
                                               @Param("deptIds") List<Long> deptIds);

    /**
     * 详情 = 离院登记/打印/审批数据源：含手写签名图与关联签名证据（承诺书注脚），
     * 电话在 SQL 出参层脱敏（详情没有编辑回显需求，明文无回写路径，AGENTS §5）。
     *
     * <p>deptIds 非空＝按当前岗位的数据范围收口；为空参数（不受限）不加条件。
     */
    @Select("""
            <script>
            SELECT l.id, l.leave_no, l.admission_id, l.patient_id, l.patient_name, l.patient_no,
                   l.gender, l.age, l.dept_id, l.dept_name, l.ward_name, l.bed_no, l.admission_no,
                   l.leave_type, l.reason, l.destination, l.companion_name, l.companion_relation,
                   CASE WHEN l.companion_phone IS NULL OR CHAR_LENGTH(l.companion_phone) &lt; 8 THEN l.companion_phone
                        ELSE CONCAT(LEFT(l.companion_phone, 3), '****', RIGHT(l.companion_phone, 4)) END AS companionPhoneMasked,
                   DATE_FORMAT(l.expected_leave_time, '%Y-%m-%d %H:%i:%s') AS expectedLeaveTime,
                   DATE_FORMAT(l.expected_return_time, '%Y-%m-%d %H:%i:%s') AS expectedReturnTime,
                   DATE_FORMAT(l.apply_time, '%Y-%m-%d %H:%i:%s') AS applyTime,
                   l.apply_by, l.doctor_advice,
                   DATE_FORMAT(l.approve_time, '%Y-%m-%d %H:%i:%s') AS approveTime,
                   l.doctor_id, l.doctor_name, l.reject_reason,
                   l.confirm_name, l.confirm_relation,
                   CASE WHEN l.confirm_phone IS NULL OR CHAR_LENGTH(l.confirm_phone) &lt; 8 THEN l.confirm_phone
                        ELSE CONCAT(LEFT(l.confirm_phone, 3), '****', RIGHT(l.confirm_phone, 4)) END AS confirmPhoneMasked,
                   l.confirm_signature,
                   DATE_FORMAT(l.confirm_time, '%Y-%m-%d %H:%i:%s') AS confirmTime,
                   DATE_FORMAT(l.actual_leave_time, '%Y-%m-%d %H:%i:%s') AS actualLeaveTime,
                   DATE_FORMAT(l.actual_return_time, '%Y-%m-%d %H:%i:%s') AS actualReturnTime,
                   l.return_note, l.return_by, l.leave_status,
                   l.overdue_contact_result, l.overdue_contact_note,
                   DATE_FORMAT(l.overdue_contact_time, '%Y-%m-%d %H:%i:%s') AS overdueContactTime,
                   l.overdue_contact_by, l.report_to,
                   l.printer_name, l.print_count,
                   DATE_FORMAT(l.last_print_time, '%Y-%m-%d %H:%i:%s') AS lastPrintTime,
                   l.cancel_reason, l.cancel_by,
                   DATE_FORMAT(l.cancel_time, '%Y-%m-%d %H:%i:%s') AS cancelTime,
                   l.sign_status, l.sign_id,
                   DATE_FORMAT(l.signed_time, '%Y-%m-%d %H:%i:%s') AS signedTime,
                   l.remark,
                   s.id            AS signRefId,
                   s.sign_no       AS signNo,
                   s.content_digest AS contentDigest,
                   s.cert_no       AS certNo,
                   DATE_FORMAT(s.signed_time, '%Y-%m-%d %H:%i:%s') AS sigSignedTime,
                   s.verify_status AS verifyStatus
              FROM biz_inpatient_leave l
              LEFT JOIN biz_emr_signature s ON s.id = l.sign_id
             WHERE l.del_flag = 0 AND l.id = #{id}
            <if test="deptIds != null">
              AND l.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </if>
            </script>
            """)
    InpatientLeaveVO.Detail selectLeaveDetail(@Param("id") Long id, @Param("deptIds") List<Long> deptIds);

    /**
     * 开单底稿：住院 + 患者一般项目 + 该住院在途请假单张数
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
                   DATE_FORMAT(a.admit_time, '%Y-%m-%d %H:%i:%s') AS admitTime,
                   (SELECT COUNT(*) FROM biz_inpatient_leave lv
                     WHERE lv.del_flag = 0 AND lv.admission_id = a.admission_id
                       AND lv.leave_status IN (1, 2, 3)) AS activeLeaveCount
              FROM biz_admission a
              JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
             WHERE a.del_flag = 0 AND a.admission_id = #{admissionId}
            """)
    InpatientLeaveVO.Base selectAdmissionBase(@Param("admissionId") Long admissionId);

    /**
     * 在院患者候选（护士站/医生站横幅数据源，含在途请假单张数与在途状态）
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
                   (SELECT COUNT(*) FROM biz_inpatient_leave lv
                     WHERE lv.del_flag = 0 AND lv.admission_id = a.admission_id
                       AND lv.leave_status IN (1, 2, 3)) AS activeLeaveCount,
                   (SELECT MAX(lv.leave_status) FROM biz_inpatient_leave lv
                     WHERE lv.del_flag = 0 AND lv.admission_id = a.admission_id
                       AND lv.leave_status = 3) AS leftStatus
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
    List<InpatientLeaveVO.Inpatient> selectInpatientCandidates(@Param("keyword") String keyword,
                                                               @Param("deptIds") List<Long> deptIds,
                                                               @Param("limit") int limit);

    /**
     * 五项统计（超期未归 = status=3 且 expected_return_time 已过，查询时算）
     */
    @Select("""
            <script>
              SELECT
              COALESCE(SUM(CASE WHEN l.leave_status = 1 THEN 1 ELSE 0 END), 0) AS pendingCount,
              COALESCE(SUM(CASE WHEN l.leave_status = 2 THEN 1 ELSE 0 END), 0) AS approvedCount,
              COALESCE(SUM(CASE WHEN l.leave_status = 3 THEN 1 ELSE 0 END), 0) AS leftCount,
              COALESCE(SUM(CASE WHEN l.leave_status = 3 AND l.expected_return_time &lt; NOW() THEN 1 ELSE 0 END), 0) AS overdueCount,
              COALESCE(SUM(CASE WHEN l.leave_status = 4 AND l.actual_return_time &gt;= CURDATE()
                                 AND l.actual_return_time &lt; DATE_ADD(CURDATE(), INTERVAL 1 DAY) THEN 1 ELSE 0 END), 0) AS returnedTodayCount
              FROM biz_inpatient_leave l
             WHERE l.del_flag = 0
            <if test="deptIds != null">
              AND l.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </if>
            </script>
            """)
    InpatientLeaveVO.Stats selectStats(@Param("deptIds") List<Long> deptIds);

    /**
     * 员工职称码（签名人 signerTitle，电子签名证据留档）
     */
    @Select("SELECT title FROM sys_employee WHERE id = #{employeeId} AND del_flag = 0")
    String selectEmployeeTitle(@Param("employeeId") Long employeeId);
}
