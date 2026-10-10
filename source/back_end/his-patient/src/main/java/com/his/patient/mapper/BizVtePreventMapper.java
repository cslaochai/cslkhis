package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.patient.dto.VtePreventQueryPageDTO;
import com.his.patient.entity.BizVtePrevent;
import com.his.patient.vo.VteMeasureStateVO;
import com.his.patient.vo.VtePreventVO;
import com.his.patient.vo.VteRiskListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * VTE 预防措施 Mapper。
 */
@Mapper
public interface BizVtePreventMapper extends BaseMapper<BizVtePrevent> {

    /**
     * 物理删除（唯一键 uk_vte_prevent 不含 del_flag，软删行会占住键位 → 必须 purge，
     * 否则第二次删同一患者的同一措施直接 Duplicate entry）。
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM biz_vte_prevent WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    @Select("""
            <script>
            SELECT v.*,
                   CASE v.measure_code WHEN 'BASIC' THEN '基础预防' WHEN 'PHYSICAL' THEN '物理预防'
                                       WHEN 'DRUG' THEN '药物预防' ELSE '未知' END AS measure_code_text,
                   CASE v.measure_type WHEN 1 THEN '基础预防' WHEN 2 THEN '物理预防'
                                       WHEN 3 THEN '药物预防' ELSE '未知' END AS measure_type_text,
                   CASE v.risk_level WHEN 1 THEN '低风险' WHEN 2 THEN '中风险' WHEN 3 THEN '高风险'
                                     WHEN 4 THEN '极高风险' ELSE '未评' END AS risk_level_text,
                   CASE v.execute_status WHEN 0 THEN '待落实' WHEN 1 THEN '已落实'
                                         WHEN 2 THEN '禁忌未用' WHEN 3 THEN '患者拒绝' ELSE '未知' END AS execute_status_text
              FROM biz_vte_prevent v
             WHERE v.del_flag = 0
               AND (#{q.admissionId} IS NULL OR v.admission_id = #{q.admissionId})
               AND (#{q.wardId} IS NULL OR v.ward_id = #{q.wardId})
               AND (#{q.executeStatus} IS NULL OR v.execute_status = #{q.executeStatus})
               AND (#{q.measureCode} IS NULL OR v.measure_code = #{q.measureCode})
               AND (#{q.keyword} IS NULL OR #{q.keyword} = '' OR v.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                    OR v.patient_no LIKE CONCAT('%', #{q.keyword}, '%'))
              <if test="deptIds != null"> AND v.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach></if>
             ORDER BY v.plan_date DESC, v.id DESC
            </script>
            """)
    IPage<VtePreventVO> selectPreventPage(Page<VtePreventVO> page,
                                          @Param("q") VtePreventQueryPageDTO query,
                                          @Param("deptIds") List<Long> deptIds);

    /**
     * 某次住院名下全部措施记录（按措施码顺序：基础→物理→药物）
     */
    @Select("""
            SELECT v.*,
                   CASE v.measure_code WHEN 'BASIC' THEN '基础预防' WHEN 'PHYSICAL' THEN '物理预防'
                                       WHEN 'DRUG' THEN '药物预防' ELSE '未知' END AS measure_code_text,
                   CASE v.measure_type WHEN 1 THEN '基础预防' WHEN 2 THEN '物理预防'
                                       WHEN 3 THEN '药物预防' ELSE '未知' END AS measure_type_text,
                   CASE v.risk_level WHEN 1 THEN '低风险' WHEN 2 THEN '中风险' WHEN 3 THEN '高风险'
                                     WHEN 4 THEN '极高风险' ELSE '未评' END AS risk_level_text,
                   CASE v.execute_status WHEN 0 THEN '待落实' WHEN 1 THEN '已落实'
                                         WHEN 2 THEN '禁忌未用' WHEN 3 THEN '患者拒绝' ELSE '未知' END AS execute_status_text
              FROM biz_vte_prevent v
             WHERE v.del_flag = 0 AND v.admission_id = #{admissionId}
             ORDER BY v.measure_type, v.id
            """)
    List<VtePreventVO> selectByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 中高危名单（风险来自每次住院最新一条 Caprini 评估 + 措施落实聚合）
     */
    @Select("""
            <script>
            SELECT a.admission_id, a.admission_no, a.patient_id, p.patient_name, p.patient_no, p.gender, p.age,
                   a.dept_id, d.dept_name, a.ward_id, w.ward_name, b.bed_no, a.admit_time, a.admit_status, a.diagnosis,
                   la.id AS assessment_id, la.total_score AS caprini_score, la.risk_level, la.assess_time,
                   la.assess_nurse_name,
                   CASE la.risk_level WHEN 1 THEN '低风险' WHEN 2 THEN '中风险' WHEN 3 THEN '高风险'
                                      WHEN 4 THEN '极高风险' ELSE '未知' END AS risk_level_text,
                   CASE WHEN la.risk_level >= 3 THEN 3 WHEN la.risk_level = 2 THEN 2 ELSE 1 END AS recommend_count,
                   COALESCE(pv.done_count, 0) AS done_count,
                   pv.last_exec_time AS latest_execute_time
              FROM biz_admission a
              JOIN (
                    SELECT t.* FROM (
                      SELECT x.*, ROW_NUMBER() OVER (PARTITION BY x.admission_id ORDER BY x.assess_time DESC, x.id DESC) rn
                        FROM biz_nursing_assessment x
                       WHERE x.del_flag = 0 AND x.assess_type = 4
                    ) t WHERE t.rn = 1
                   ) la ON la.admission_id = a.admission_id
              LEFT JOIN biz_patient p ON p.id = a.patient_id
              LEFT JOIN sys_department d ON d.id = a.dept_id
              LEFT JOIN sys_ward w ON w.ward_id = a.ward_id
              LEFT JOIN sys_bed b ON b.bed_id = a.bed_id
              LEFT JOIN (
                    SELECT admission_id, SUM(execute_status = 1) AS done_count, COUNT(*) AS total_count,
                           MAX(execute_time) AS last_exec_time
                      FROM biz_vte_prevent WHERE del_flag = 0 GROUP BY admission_id
                   ) pv ON pv.admission_id = a.admission_id
             WHERE a.del_flag = 0
               AND (COALESCE(#{q.onlyHighRisk}, 0) = 0 OR la.risk_level >= 2)
               AND (#{q.admitStatus} IS NULL OR a.admit_status = #{q.admitStatus})
               AND (#{q.wardId} IS NULL OR a.ward_id = #{q.wardId})
               AND (#{q.deptId} IS NULL OR a.dept_id = #{q.deptId})
               AND (#{q.riskLevel} IS NULL OR la.risk_level = #{q.riskLevel})
               AND (#{q.keyword} IS NULL OR #{q.keyword} = '' OR p.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                    OR p.patient_no LIKE CONCAT('%', #{q.keyword}, '%') OR a.admission_no LIKE CONCAT('%', #{q.keyword}, '%'))
              <if test="deptIds != null"> AND a.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach></if>
               <choose>
                 <when test="q.preventStatus != null and q.preventStatus == 0">
                   AND COALESCE(pv.done_count, 0) = 0
                 </when>
                 <when test="q.preventStatus != null and q.preventStatus == 1">
                   AND COALESCE(pv.done_count, 0) &gt; 0
                   AND (CASE WHEN la.risk_level &gt;= 3 THEN 3 WHEN la.risk_level = 2 THEN 2 ELSE 1 END
                        - COALESCE(pv.done_count, 0)) &gt; 0
                 </when>
                 <when test="q.preventStatus != null and q.preventStatus == 2">
                   AND COALESCE(pv.done_count, 0) &gt;=
                       (CASE WHEN la.risk_level &gt;= 3 THEN 3 WHEN la.risk_level = 2 THEN 2 ELSE 1 END)
                 </when>
               </choose>
             ORDER BY la.risk_level DESC, la.total_score DESC, a.admission_id DESC
            </script>
            """)
    IPage<VteRiskListVO> selectRiskPage(Page<VteRiskListVO> page,
                                        @Param("q") com.his.patient.dto.VteRiskQueryPageDTO query,
                                        @Param("deptIds") List<Long> deptIds);

    /**
     * 名单行的措施状态明细（一次查回多行，服务端拼到对应 admission 上）
     */
    @Select("""
            <script>
            SELECT v.admission_id, v.measure_code, v.measure_name, v.measure_type,
                   CASE v.measure_type WHEN 1 THEN '基础预防' WHEN 2 THEN '物理预防'
                                       WHEN 3 THEN '药物预防' ELSE '未知' END AS measure_type_text,
                   v.execute_status,
                   CASE v.execute_status WHEN 0 THEN '待落实' WHEN 1 THEN '已落实'
                                         WHEN 2 THEN '禁忌未用' WHEN 3 THEN '患者拒绝' ELSE '未知' END AS execute_status_text,
                   v.executor_name, v.execute_time, v.reason
              FROM biz_vte_prevent v
             WHERE v.del_flag = 0 AND v.admission_id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
             ORDER BY v.admission_id, v.measure_type
            </script>
            """)
    List<VteMeasureStateVO> selectMeasureStates(@Param("ids") List<Long> ids);
}
