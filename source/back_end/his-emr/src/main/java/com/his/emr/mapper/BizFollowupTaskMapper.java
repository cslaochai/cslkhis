package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizFollowupTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 随访任务Mapper
 */
@Mapper
public interface BizFollowupTaskMapper extends BaseMapper<BizFollowupTask> {

    /**
     * 患者快照（跨模块裸 SQL，患者基本信息列名已对 information_schema 核对）
     */
    @Select("SELECT id, patient_no AS patientNo, patient_name AS patientName, phone AS phone, "
            + "last_visit_dept AS deptId, last_visit_dept_name AS deptName "
            + "FROM biz_patient WHERE id = #{patientId} AND del_flag = 0 LIMIT 1")
    Map<String, Object> selectPatientSnapshot(@Param("patientId") Long patientId);

    /**
     * 出院记录快照（跨模块裸 SQL：出院记录/入院记录/患者基本信息/科室，列名已核对）。
     * discharge 与 admission 都有 patient_id，用 discharge 的；discharge_no 也带回做幂等展示。
     *
     * <p>科室取住院就诊科室（入院记录的科室ID）：随访归属必须落在病人实际住院的那个回答，
     * 出院记录本身没有科室列，用出院时的「最后就诊科室」会把科室随登记时间漂移。
     */
    @Select("SELECT d.discharge_id AS dischargeId, d.discharge_no AS dischargeNo, d.admission_id AS admissionId, "
            + "d.patient_id AS patientId, "
            /* DATETIME 一律 DATE_FORMAT 输出文本：mysql2/mybatis 裸 Map 取值拿到的是各自日期对象，强转必炸 */
            + "DATE_FORMAT(d.discharge_time, '%Y-%m-%d %H:%i:%s') AS dischargeTime, "
            + "IFNULL(d.discharge_diagnosis, a.diagnosis) AS diagnosis, "
            + "a.dept_id AS deptId, sd.dept_name AS deptName, "
            + "p.patient_no AS patientNo, p.patient_name AS patientName, p.phone AS phone "
            + "FROM biz_discharge d "
            + "JOIN biz_patient p ON p.id = d.patient_id AND p.del_flag = 0 "
            + "LEFT JOIN biz_admission a ON a.admission_id = d.admission_id "
            + "LEFT JOIN sys_department sd ON sd.id = a.dept_id AND sd.del_flag = 0 "
            + "WHERE d.discharge_id = #{dischargeId} AND d.del_flag = 0 LIMIT 1")
    Map<String, Object> selectDischargeSnapshot(@Param("dischargeId") Long dischargeId);

    /**
     * 待补建随访计划的出院记录（存活出院且尚无对应任务，按出院先后取前 N 条）。
     *
     * <p>判据与建单侧用同一个幂等锚，所以重复扫描只会补真正欠的那几条；
     * 手术与否在这一条 SQL 里一次带出（有手术排术后随访，否则排复诊提醒），免得逐条回查。
     */
    @Select("""
            SELECT d.discharge_id AS dischargeId,
                   CASE WHEN EXISTS (SELECT 1 FROM biz_inpatient_operation o
                                      WHERE o.admission_id = d.admission_id AND o.del_flag = 0)
                        THEN 1 ELSE 0 END AS hasOperation
              FROM biz_discharge d
             WHERE d.del_flag = 0 AND d.discharge_status = 1 AND IFNULL(d.death_flag, 0) <> 1
               AND NOT EXISTS (SELECT 1 FROM biz_followup_task t
                                WHERE t.del_flag = 0
                                  AND t.remark LIKE CONCAT('G20-FUV:', d.discharge_id, '|%'))
             ORDER BY d.discharge_time ASC, d.discharge_id ASC
             LIMIT #{limit}
            """)
    List<Map<String, Object>> selectDischargesWithoutTask(@Param("limit") int limit);

    /**
     * 科室名快照（科室跨模块裸 SQL，列名已核对）
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    /**
     * 看板：状态分布 + 今日应随访 + 逾期未随访 + 完成率原料。
     *
     * <p>逾期一律按 followup_time 与 NOW() 现算，不建定时任务翻状态 ——
     * 「日期已过、状态还没刷」的漂移窗口会让看板每天都和昨晚的批处理不一样。
     */
    @Select("""
            <script>
            SELECT COUNT(CASE WHEN t.followup_status = 1 THEN 1 END) AS pending,
                   COUNT(CASE WHEN t.followup_status = 2 THEN 1 END) AS doing,
                   COUNT(CASE WHEN t.followup_status = 3 THEN 1 END) AS done,
                   COUNT(CASE WHEN t.followup_status = 4 THEN 1 END) AS cancelled,
                   COUNT(CASE WHEN t.followup_status IN (1, 2) AND DATE(t.followup_time) = CURDATE()
                        THEN 1 END) AS today_due,
                   COUNT(CASE WHEN t.followup_status IN (1, 2) AND t.followup_time &lt; NOW()
                        THEN 1 END) AS overdue,
                   COUNT(CASE WHEN t.followup_status = 3 AND DATE(t.execute_time) = CURDATE()
                        THEN 1 END) AS done_today,
                   COUNT(CASE WHEN t.revisit_appoint_id IS NOT NULL THEN 1 END) AS revisit_cnt,
                   COUNT(*) AS total
              FROM biz_followup_task t
             WHERE t.del_flag = 0
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND t.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
            </script>
            """)
    Map<String, Object> statOverview(@Param("scopeDeptIds") List<Long> scopeDeptIds);

    /**
     * 看板：按随访方式分布（字典 his_followup_type）
     */
    @Select("""
            <script>
            SELECT t.followup_type AS k, COUNT(*) AS c
              FROM biz_followup_task t
             WHERE t.del_flag = 0
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND t.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
             GROUP BY t.followup_type ORDER BY c DESC
            </script>
            """)
    List<Map<String, Object>> countByType(@Param("scopeDeptIds") List<Long> scopeDeptIds);

    /**
     * 看板：科室待办 TOP10（未完成口径 = 待随访+随访中）。
     *
     * <p>随访的瓶颈从来不是「谁做完了」而是「谁的欠账在堆积」，光荣榜对管理毫无用处。
     *
     * <p>科室名以科室为准、快照兜底：只信 dept_name 的话，dept_id 指向已撤科室
     * 或快照没回填的行会和「根本没科室」挤成同一个「未指定科室」，两个榜并列在屏幕上
     * 却互不相干 —— 前者要人去补科室，后者是历史遗留，看板必须分得开。
     */
    @Select("""
            <script>
            SELECT COALESCE(t.dept_id, 0) AS d,
                   COALESCE(NULLIF(dep.dept_name, ''), NULLIF(t.dept_name, ''),
                            CASE WHEN t.dept_id IS NULL THEN '未指定科室' ELSE CONCAT('科室#', t.dept_id) END) AS n,
                   COUNT(*) AS c,
                   COUNT(CASE WHEN t.followup_status = 3 THEN 1 END) AS done
              FROM biz_followup_task t
              LEFT JOIN sys_department dep ON dep.id = t.dept_id AND dep.del_flag = 0
             WHERE t.del_flag = 0 AND t.followup_status IN (1, 2)
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND t.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
             GROUP BY COALESCE(t.dept_id, 0), n
             ORDER BY c DESC, d ASC LIMIT 10
            </script>
            """)
    List<Map<String, Object>> countDeptPending(@Param("scopeDeptIds") List<Long> scopeDeptIds);
}
