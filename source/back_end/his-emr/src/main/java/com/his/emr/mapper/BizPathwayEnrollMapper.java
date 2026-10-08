package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.emr.entity.BizPathwayEnroll;
import com.his.emr.vo.PathwayAdmissionVO;
import com.his.emr.vo.PathwayEnrollVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 临床路径入径记录 Mapper。
 */
@Mapper
public interface BizPathwayEnrollMapper extends BaseMapper<BizPathwayEnroll> {

    @Select("""
            <script>
            SELECT e.*
              FROM biz_pathway_enroll e
             WHERE e.del_flag = 0
               <if test="pathwayId != null"> AND e.pathway_id = #{pathwayId}</if>
               <if test="deptId != null"> AND e.dept_id = #{deptId}</if>
               <if test="status != null"> AND e.status = #{status}</if>
               <if test="enrollDate != null"> AND e.enroll_date = #{enrollDate}</if>
               <if test="patientName != null and patientName != ''">
                 AND e.patient_name LIKE CONCAT('%', #{patientName}, '%')
               </if>
             ORDER BY e.status ASC, e.enroll_date DESC, e.id DESC
            </script>
            """)
    List<PathwayEnrollVO> selectEnrollPage(IPage<PathwayEnrollVO> page,
                                           @Param("pathwayId") Long pathwayId,
                                           @Param("deptId") Long deptId,
                                           @Param("status") Integer status,
                                           @Param("enrollDate") LocalDate enrollDate,
                                           @Param("patientName") String patientName);

    @Select("SELECT e.* FROM biz_pathway_enroll e WHERE e.id = #{id} AND e.del_flag = 0")
    PathwayEnrollVO selectEnrollById(@Param("id") Long id);

    /**
     * 该次住院是否已有在径记录（一次住院同时仅一条在径）
     */
    @Select("SELECT COUNT(*) FROM biz_pathway_enroll WHERE del_flag = 0 AND status = 1 AND admission_id = #{admissionId}")
    int countActiveByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 该次住院的在径记录（医生站横幅/开单预检用，至多一条）
     */
    @Select("SELECT * FROM biz_pathway_enroll WHERE del_flag = 0 AND status = 1 AND admission_id = #{admissionId} LIMIT 1")
    BizPathwayEnroll selectActiveByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 可入径候选：在院患者（admit_status=1）。已排除已有在径记录的住院。
     */
    @Select("""
            <script>
            SELECT a.admission_id AS admissionId,
                   a.admission_no AS admissionNo,
                   a.patient_id   AS patientId,
                   pt.patient_no  AS patientNo,
                   pt.patient_name AS patientName,
                   a.dept_id      AS deptId,
                   (SELECT d.dept_name FROM sys_department d WHERE d.id = a.dept_id) AS deptName,
                   a.diagnosis    AS diagnosis,
                   DATE_FORMAT(a.admit_time, '%Y-%m-%d %H:%i:%s') AS admitTime
              FROM biz_admission a
              JOIN biz_patient pt ON pt.id = a.patient_id AND pt.del_flag = 0
             WHERE a.del_flag = 0 AND a.admit_status = 1
               AND NOT EXISTS (SELECT 1 FROM biz_pathway_enroll e
                                WHERE e.del_flag = 0 AND e.status = 1 AND e.admission_id = a.admission_id)
               <if test="keyword != null and keyword != ''">
                 AND (pt.patient_name LIKE CONCAT('%', #{keyword}, '%')
                   OR pt.patient_no LIKE CONCAT('%', #{keyword}, '%')
                   OR a.admission_no LIKE CONCAT('%', #{keyword}, '%'))
               </if>
             ORDER BY a.admit_time DESC
             LIMIT #{limit}
            </script>
            """)
    List<PathwayAdmissionVO> selectAdmissionCandidates(@Param("keyword") String keyword,
                                                       @Param("limit") int limit);

    /**
     * 入院快照（入径写库前按 admission_id 取，不信前端传来的患者/科室信息）
     */
    @Select("""
            SELECT a.admission_id AS admissionId,
                   a.admission_no AS admissionNo,
                   a.patient_id   AS patientId,
                   pt.patient_no  AS patientNo,
                   pt.patient_name AS patientName,
                   a.dept_id      AS deptId,
                   (SELECT d.dept_name FROM sys_department d WHERE d.id = a.dept_id) AS deptName,
                   a.diagnosis    AS diagnosis,
                   a.admit_status AS admitStatus,
                   DATE_FORMAT(a.admit_time, '%Y-%m-%d %H:%i:%s') AS admitTime
              FROM biz_admission a
              JOIN biz_patient pt ON pt.id = a.patient_id AND pt.del_flag = 0
             WHERE a.del_flag = 0 AND a.admission_id = #{admissionId}
            """)
    PathwayAdmissionVO selectAdmissionSnapshot(@Param("admissionId") Long admissionId);
}
