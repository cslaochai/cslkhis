package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.entity.BizDialysisPatient;
import com.his.medicaltech.vo.DialysisVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 透析患者档案 Mapper。
 */
@Mapper
public interface BizDialysisPatientMapper extends BaseMapper<BizDialysisPatient> {

    String ARCHIVE_COLUMNS = """
             p.*,
             ap.id            AS active_prescription_id,
             ap.dry_weight    AS dry_weight,
             ap.duration_min  AS duration_min,
             ap.blood_flow    AS blood_flow,
             ap.dialyzer      AS dialyzer,
             ap.anticoagulant AS anticoagulant,
             (SELECT COUNT(*) FROM biz_dialysis_session s
               WHERE s.del_flag = 0 AND s.archive_id = p.id) AS session_total,
             (SELECT COUNT(*) FROM biz_dialysis_session s
               WHERE s.del_flag = 0 AND s.status = 3 AND s.archive_id = p.id) AS session_done,
             (SELECT MAX(s.dialysis_date) FROM biz_dialysis_session s
               WHERE s.del_flag = 0 AND s.archive_id = p.id) AS last_session_date
            """;

    @Select("""
            <script>
            SELECT """ + ARCHIVE_COLUMNS + """
              FROM biz_dialysis_patient p
              LEFT JOIN biz_dialysis_prescription ap
                     ON ap.archive_id = p.id AND ap.del_flag = 0 AND ap.status = 1
             WHERE p.del_flag = 0
               <if test="dialysisNo != null and dialysisNo != ''">
                 AND p.dialysis_no LIKE CONCAT('%', #{dialysisNo}, '%')
               </if>
               <if test="patientName != null and patientName != ''">
                 AND p.patient_name LIKE CONCAT('%', #{patientName}, '%')
               </if>
               <if test="accessType != null"> AND p.access_type = #{accessType}</if>
               <if test="status != null"> AND p.status = #{status}</if>
             ORDER BY p.status ASC, p.update_time DESC, p.id DESC
            </script>
            """)
    List<DialysisVO.ArchiveVO> selectArchivePage(IPage<DialysisVO.ArchiveVO> page,
                                                 @Param("dialysisNo") String dialysisNo,
                                                 @Param("patientName") String patientName,
                                                 @Param("accessType") Integer accessType,
                                                 @Param("status") Integer status);

    @Select("""
            SELECT """ + ARCHIVE_COLUMNS + """
              FROM biz_dialysis_patient p
              LEFT JOIN biz_dialysis_prescription ap
                     ON ap.archive_id = p.id AND ap.del_flag = 0 AND ap.status = 1
             WHERE p.del_flag = 0 AND p.id = #{id}
            """)
    DialysisVO.ArchiveVO selectArchiveById(@Param("id") Long id);

    /**
     * 该患者是否已建档（一人一档，撞 uk_dp_patient 前先给业务提示）
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_dialysis_patient
             WHERE del_flag = 0 AND patient_id = #{patientId}
               <if test="id != null"> AND id &lt;&gt; #{id}</if>
            </script>
            """)
    int countByPatient(@Param("patientId") Long patientId, @Param("id") Long id);

    /**
     * 患者主档快照（建档时服务端重查，不信前端传来的姓名/电话）
     */
    @Select("""
            SELECT id           AS patientId,
                   patient_no   AS patientNo,
                   patient_name AS patientName,
                   phone        AS phone
              FROM biz_patient
             WHERE id = #{patientId} AND del_flag = 0
            """)
    DialysisVO.ArchiveVO selectPatientSnapshot(@Param("patientId") Long patientId);

    /**
     * 在透患者数（统计用，可按首透日期区间筛）
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_dialysis_patient p
             WHERE p.del_flag = 0 AND p.status = 1
               <if test="startDate != null"> AND p.first_dialysis_date &lt;= #{startDate}</if>
            </script>
            """)
    int countInDialysis(@Param("startDate") LocalDate startDate);
}
