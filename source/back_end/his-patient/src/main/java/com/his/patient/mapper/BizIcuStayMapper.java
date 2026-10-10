package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizIcuStay;
import com.his.patient.vo.IcuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ICU 入出科 Mapper。
 */
@Mapper
public interface BizIcuStayMapper extends BaseMapper<BizIcuStay> {

    String STAY_COLUMNS = """
             s.*,
             (SELECT MAX(m.record_time) FROM biz_icu_monitor m
               WHERE m.del_flag = 0 AND m.stay_id = s.id) AS last_monitor_time,
             TIMESTAMPDIFF(HOUR, s.in_time, COALESCE(s.out_time, NOW())) AS stay_hours
            """;

    @Select("""
            <script>
            SELECT """ + STAY_COLUMNS + """
              FROM biz_icu_stay s
             WHERE s.del_flag = 0
               <if test="stayNo != null and stayNo != ''">
                 AND s.stay_no LIKE CONCAT('%', #{stayNo}, '%')
               </if>
               <if test="patientName != null and patientName != ''">
                 AND s.patient_name LIKE CONCAT('%', #{patientName}, '%')
               </if>
               <if test="startDate != null"> AND s.in_time &gt;= #{startDate}</if>
               <if test="endDate != null"> AND s.in_time &lt;= CONCAT(#{endDate}, ' 23:59:59')</if>
               <if test="wardId != null"> AND s.ward_id = #{wardId}</if>
               <if test="wardIds != null"> AND s.ward_id IN <foreach collection="wardIds" item="w" open="(" separator="," close=")">#{w}</foreach></if>
               <if test="careLevel != null"> AND s.care_level = #{careLevel}</if>
               <if test="status != null"> AND s.status = #{status}</if>
             ORDER BY s.status ASC, s.in_time DESC, s.id DESC
            </script>
            """)
    List<IcuVO.StayVO> selectStayPage(IPage<IcuVO.StayVO> page,
                                      @Param("stayNo") String stayNo,
                                      @Param("patientName") String patientName,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("wardId") Long wardId,
                                      @Param("wardIds") List<Long> wardIds,
                                      @Param("careLevel") Integer careLevel,
                                      @Param("status") Integer status);

    @Select("""
            SELECT """ + STAY_COLUMNS + """
              FROM biz_icu_stay s
             WHERE s.del_flag = 0 AND s.id = #{id}
            """)
    IcuVO.StayVO selectStayById(@Param("id") Long id);

    /**
     * 该次住院是否已有在科记录（一次住院同时仅一条在科）
     */
    @Select("SELECT COUNT(*) FROM biz_icu_stay WHERE del_flag = 0 AND status = 1 AND admission_id = #{admissionId}")
    int countActiveByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 该床是否已被其他在科患者占用
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_icu_stay
             WHERE del_flag = 0 AND status = 1 AND bed_id = #{bedId}
               <if test="excludeId != null"> AND id &lt;&gt; #{excludeId}</if>
            </script>
            """)
    int countActiveByBed(@Param("bedId") Long bedId, @Param("excludeId") Long excludeId);

    /**
     * 可入科候选：在院（admit_status=1）且当前无在科记录
     */
    @Select("""
            <script>
            SELECT a.admission_id   AS admissionId,
                   a.admission_no   AS admissionNo,
                   a.patient_id     AS patientId,
                   p.patient_no     AS patientNo,
                   p.patient_name   AS patientName,
                   p.gender         AS gender,
                   p.age            AS age,
                   a.dept_id        AS deptId,
                   (SELECT d.dept_name FROM sys_department d WHERE d.id = a.dept_id) AS deptName,
                   (SELECT b.bed_no FROM sys_bed b WHERE b.bed_id = a.bed_id AND b.del_flag = 0) AS bedNo,
                   a.diagnosis      AS diagnosis,
                   a.admit_time     AS admitTime
              FROM biz_admission a
              JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
             WHERE a.del_flag = 0 AND a.admit_status = 1
               AND NOT EXISTS (SELECT 1 FROM biz_icu_stay s
                                WHERE s.del_flag = 0 AND s.status = 1 AND s.admission_id = a.admission_id)
               <if test="keyword != null and keyword != ''">
                 AND (p.patient_name LIKE CONCAT('%', #{keyword}, '%')
                   OR p.patient_no LIKE CONCAT('%', #{keyword}, '%')
                   OR a.admission_no LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="deptIds != null"> AND a.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach></if>
             ORDER BY a.admit_time DESC
             LIMIT #{limit}
            </script>
            """)
    List<IcuVO.AdmissionVO> selectAdmissionCandidates(@Param("keyword") String keyword,
                                                      @Param("deptIds") List<Long> deptIds,
                                                      @Param("limit") int limit);

    /**
     * 入院快照（写库前按 admissionId 重查，不信前端传来的患者信息）
     */
    @Select("""
            SELECT a.admission_id   AS admissionId,
                   a.admission_no   AS admissionNo,
                   a.patient_id     AS patientId,
                   p.patient_no     AS patientNo,
                   p.patient_name   AS patientName,
                   a.dept_id        AS deptId,
                   (SELECT d.dept_name FROM sys_department d WHERE d.id = a.dept_id) AS deptName,
                   a.diagnosis      AS diagnosis,
                   a.admit_status   AS admitStatus,
                   a.admit_time     AS admitTime
              FROM biz_admission a
              JOIN biz_patient p ON p.id = a.patient_id AND p.del_flag = 0
             WHERE a.del_flag = 0 AND a.admission_id = #{admissionId}
            """)
    IcuVO.AdmissionVO selectAdmissionSnapshot(@Param("admissionId") Long admissionId);

    /**
     * ICU 床位一览（含在科患者与最近一次监护读数）
     */
    @Select("""
            <script>
            SELECT b.bed_id          AS bedId,
                   b.bed_no          AS bedNo,
                   b.bed_status      AS bedStatus,
                   s.id              AS stayId,
                   s.stay_no         AS stayNo,
                   s.patient_id      AS patientId,
                   s.patient_no      AS patientNo,
                   s.patient_name    AS patientName,
                   s.care_level      AS careLevel,
                   s.in_time         AS inTime,
                   s.in_gcs          AS inGcs,
                   s.monitor_count   AS monitorCount,
                   lm.record_time    AS lastMonitorTime,
                   lm.sbp            AS lastSbp,
                   lm.dbp            AS lastDbp,
                   lm.pulse          AS lastPulse,
                   lm.spo2           AS lastSpo2,
                   lm.temperature    AS lastTemperature,
                   lm.gcs_total      AS lastGcsTotal
              FROM sys_bed b
              LEFT JOIN biz_icu_stay s ON s.bed_id = b.bed_id AND s.del_flag = 0 AND s.status = 1
              LEFT JOIN biz_icu_monitor lm
                     ON lm.id = (SELECT m2.id FROM biz_icu_monitor m2
                                  WHERE m2.del_flag = 0 AND m2.stay_id = s.id
                                  ORDER BY m2.record_time DESC, m2.id DESC LIMIT 1)
             WHERE b.del_flag = 0 AND b.bed_type = 'ICU'
               <if test="wardId != null"> AND b.ward_id = #{wardId}</if>
               <if test="wardIds != null"> AND b.ward_id IN <foreach collection="wardIds" item="w" open="(" separator="," close=")">#{w}</foreach></if>
             ORDER BY b.ward_id ASC, b.bed_no ASC
            </script>
            """)
    List<IcuVO.BedVO> selectBedBoard(@Param("wardId") Long wardId,
                                     @Param("wardIds") List<Long> wardIds);

    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_icu_stay s
             WHERE s.del_flag = 0 AND s.status = 1
               <if test="wardIds != null"> AND s.ward_id IN <foreach collection="wardIds" item="w" open="(" separator="," close=")">#{w}</foreach></if>
            </script>
            """)
    int countInDept(@Param("wardIds") List<Long> wardIds);

    /**
     * 床位快照（入科写库前校验是 ICU 床并取病区名，不信前端传的床位信息）
     */
    @Select("""
            SELECT b.bed_id       AS bedId,
                   b.bed_no       AS bedNo,
                   b.bed_type     AS bedType,
                   b.bed_status   AS bedStatus,
                   b.ward_id      AS wardId,
                   w.ward_name    AS wardName
              FROM sys_bed b
              LEFT JOIN sys_ward w ON w.ward_id = b.ward_id
             WHERE b.del_flag = 0 AND b.bed_id = #{bedId}
            """)
    IcuVO.BedVO selectBedSnapshot(@Param("bedId") Long bedId);

    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_icu_stay s
             WHERE s.del_flag = 0 AND s.status = 1
               AND s.in_time &lt;= DATE_SUB(NOW(), INTERVAL #{lagHours} HOUR)
               AND NOT EXISTS (SELECT 1 FROM biz_icu_monitor m
                                WHERE m.del_flag = 0 AND m.stay_id = s.id
                                  AND m.record_time &gt;= DATE_SUB(NOW(), INTERVAL #{lagHours} HOUR))
               <if test="wardIds != null"> AND s.ward_id IN <foreach collection="wardIds" item="w" open="(" separator="," close=")">#{w}</foreach></if>
            </script>
            """)
    int countMonitorLag(@Param("lagHours") int lagHours, @Param("wardIds") List<Long> wardIds);

    /**
     * 区间入出科汇总（含平均滞留小时与死亡数）
     */
    @Select("""
            <script>
            SELECT SUM(CASE WHEN s.in_time BETWEEN #{startDateTime} AND #{endDateTime} THEN 1 ELSE 0 END) AS in_count_range,
                   SUM(CASE WHEN s.status = 2 AND s.out_time BETWEEN #{startDateTime} AND #{endDateTime} THEN 1 ELSE 0 END) AS out_count_range,
                   ROUND(AVG(CASE WHEN s.status = 2 AND s.out_time BETWEEN #{startDateTime} AND #{endDateTime}
                                  THEN TIMESTAMPDIFF(MINUTE, s.in_time, s.out_time) / 60 END), 1) AS avg_stay_hours,
                   SUM(CASE WHEN s.status = 2 AND s.out_dest = 5 AND s.out_time BETWEEN #{startDateTime} AND #{endDateTime}
                            THEN 1 ELSE 0 END) AS death_count
              FROM biz_icu_stay s
             WHERE s.del_flag = 0
               <if test="wardIds != null"> AND s.ward_id IN <foreach collection="wardIds" item="w" open="(" separator="," close=")">#{w}</foreach></if>
            </script>
            """)
    IcuVO.StatsVO selectRangeSummary(@Param("startDateTime") LocalDateTime startDateTime,
                                     @Param("endDateTime") LocalDateTime endDateTime,
                                     @Param("wardIds") List<Long> wardIds);

    @Select("""
            <script>
            SELECT s.care_level AS type, COUNT(*) AS count
              FROM biz_icu_stay s
             WHERE s.del_flag = 0 AND s.status = 1
               <if test="wardIds != null"> AND s.ward_id IN <foreach collection="wardIds" item="w" open="(" separator="," close=")">#{w}</foreach></if>
             GROUP BY s.care_level
             ORDER BY s.care_level ASC
            </script>
            """)
    List<IcuVO.TypeCount> selectCareLevelBoard(@Param("wardIds") List<Long> wardIds);

    /**
     * 病区所属科室ID（数据权限折算用：ward_id 是 sys_ward 主键不是科室ID；取不到返回 NULL）
     */
    @Select("SELECT dept_id FROM sys_ward WHERE ward_id = #{wardId}")
    Long selectWardDeptId(@Param("wardId") Long wardId);

    /**
     * 授权科室集合折算成可见病区ID集合（数据权限收口；科室没绑病区时返回空集合）
     */
    @Select("""
            <script>
            SELECT ward_id FROM sys_ward
             WHERE dept_id IN
            <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </script>
            """)
    List<Long> selectWardIdsByDeptIds(@Param("deptIds") List<Long> deptIds);
}
