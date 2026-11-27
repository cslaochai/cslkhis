package com.his.inpatient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.inpatient.dto.InpatientRecordPageQueryDTO;
import com.his.inpatient.entity.BizInpatientRecord;
import com.his.inpatient.vo.InpatOverdueDraftVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住院病历文书 Mapper。
 */
@Mapper
public interface BizInpatientRecordMapper extends BaseMapper<BizInpatientRecord> {

    /**
     * 病历文书分页（返回实体，结构化率由服务层按统一口径计算）
     * <p>{@code scopeDeptIds} 是科室数据权限（M6）的服务端收敛集合，前端不可见。
     */
    @Select("""
            <script>
            SELECT r.*
            FROM biz_inpatient_record r
            WHERE r.del_flag = 0
              AND (#{q.admissionId} IS NULL OR r.admission_id = #{q.admissionId})
              AND (#{q.patientId} IS NULL OR r.patient_id = #{q.patientId})
              AND (#{q.recordType} IS NULL OR r.record_type = #{q.recordType})
              AND (#{q.recordStatus} IS NULL OR r.record_status = #{q.recordStatus})
              <if test="q.scopeDeptIds != null and q.scopeDeptIds.size() > 0">
                AND r.dept_id IN
                <foreach collection="q.scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
              </if>
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR r.record_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.record_title LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.chief_complaint LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.diagnosis_name LIKE CONCAT('%', #{q.keyword}, '%'))
            ORDER BY r.record_time DESC, r.id DESC
            </script>
            """)
    IPage<BizInpatientRecord> selectRecordPage(IPage<BizInpatientRecord> page,
                                               @Param("q") InpatientRecordPageQueryDTO query);

    /**
     * 某次住院的全部文书（结构化率统计用，不分页）
     */
    @Select("""
            SELECT r.* FROM biz_inpatient_record r
            WHERE r.del_flag = 0 AND r.admission_id = #{admissionId}
            ORDER BY r.record_type ASC, r.record_time ASC, r.id ASC
            """)
    List<BizInpatientRecord> selectByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 某次住院的文书号集合（日志按住院查询时先取号，再按号过滤日志）
     */
    @Select("""
            SELECT r.record_no FROM biz_inpatient_record r
            WHERE r.del_flag = 0 AND r.admission_id = #{admissionId}
            """)
    List<String> selectRecordNosByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 时限催办候选：草稿状态且属规范时限文书类型（入院记录/首程/手术记录/出院记录/转入记录）。
     * 基线时刻按类型回表取（入院→admit_time；手术→operation_date；出院→discharge_time；转入→receive_time），
     * 是否真正超时由服务层按「基线 + 规范时限」判定，避免 SQL 内散落时限口径。
     */
    @Select("""
            SELECT r.id AS recordId, r.record_no AS recordNo, r.record_type AS recordType,
                   r.record_title AS recordTitle, r.patient_name AS patientName,
                   r.dept_id AS deptId, r.dept_name AS deptName,
                   r.doctor_id AS doctorId, r.doctor_name AS doctorName, r.admission_id AS admissionId,
                   CASE r.record_type
                       WHEN 1 THEN a.admit_time
                       WHEN 2 THEN a.admit_time
                       WHEN 5 THEN op.operation_date
                       WHEN 7 THEN d.discharge_time
                       WHEN 10 THEN t.receive_time
                   END AS baselineTime,
                   CASE r.record_type
                       WHEN 1 THEN 24 WHEN 2 THEN 8 WHEN 5 THEN 24 WHEN 7 THEN 24 WHEN 10 THEN 24
                   END AS deadlineHours
            FROM biz_inpatient_record r
            LEFT JOIN biz_admission a ON a.admission_id = r.admission_id AND a.del_flag = 0
            LEFT JOIN (SELECT admission_id, MAX(operation_date) AS operation_date
                       FROM biz_inpatient_operation WHERE del_flag = 0 GROUP BY admission_id) op
                   ON op.admission_id = r.admission_id
            LEFT JOIN (SELECT admission_id, MAX(discharge_time) AS discharge_time
                       FROM biz_discharge WHERE del_flag = 0 GROUP BY admission_id) d
                   ON d.admission_id = r.admission_id
            LEFT JOIN (SELECT admission_id, MAX(receive_time) AS receive_time
                       FROM biz_inpatient_transfer WHERE del_flag = 0 GROUP BY admission_id) t
                   ON t.admission_id = r.admission_id
            WHERE r.del_flag = 0
              AND r.record_status = 1
              AND r.record_type IN (1, 2, 5, 7, 10)
            ORDER BY baselineTime ASC
            LIMIT #{limit}
            """)
    List<InpatOverdueDraftVO> selectOverdueDraftCandidates(@Param("limit") int limit);

}
