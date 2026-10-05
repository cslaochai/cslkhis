package com.his.patient.mapper;

import com.his.patient.vo.DeptCountRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * VTE 防控指标复算（全部裸 SQL）。
 *
 * <p><b>为什么全是裸 SQL</b>：指标要跨入院记录 / 护理评估单（his-patient）、
 * 患者基本信息（his-patient）、科室 / 病区（his-system）多张表，
 * 而且要取"每次住院最新一条评估"这种窗口函数 —— 走实体关联会把分页插件、逻辑删除、
 * 快照过滤搅在一起，口径难以一眼看全；裸 SQL 把口径摊在明面上，改口径只改这一处。
 *
 * <p><b>分母口径</b>：同期出院患者（discharge_time 落在统计月内），与抗菌药物监测 sql/161
 * 的"按出院归月"保持一致，避免全院两套分母对不上账。
 */
@Mapper
public interface VteStatMapper {

    /**
     * 同期出院患者数（所有比率的分母）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.discharge_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
            """)
    long countDischarge(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("deptId") Long deptId);

    /**
     * 出院患者中做过 Caprini 评估（assess_type=4）的人数
     */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.discharge_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
               AND EXISTS (SELECT 1 FROM biz_nursing_assessment x
                            WHERE x.del_flag = 0 AND x.assess_type = 4 AND x.admission_id = a.admission_id)
            """)
    long countAssessed(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("deptId") Long deptId);

    /**
     * 出院患者中最新一条 Caprini 评估为中高危（risk_level >= 2）的人数。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.discharge_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
               AND EXISTS (SELECT 1 FROM (
                        SELECT t.* FROM (
                          SELECT x.*, ROW_NUMBER() OVER (PARTITION BY x.admission_id
                                 ORDER BY x.assess_time DESC, x.id DESC) rn
                            FROM biz_nursing_assessment x
                           WHERE x.del_flag = 0 AND x.assess_type = 4
                        ) t WHERE t.rn = 1
                     ) la WHERE la.admission_id = a.admission_id AND la.risk_level >= 2)
            """)
    long countHighRisk(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("deptId") Long deptId);

    /**
     * 中高危患者中至少落实一条措施（execute_status=1）的人数 —— 落实率分子
     */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.discharge_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
               AND EXISTS (SELECT 1 FROM (
                        SELECT t.* FROM (
                          SELECT x.*, ROW_NUMBER() OVER (PARTITION BY x.admission_id
                                 ORDER BY x.assess_time DESC, x.id DESC) rn
                            FROM biz_nursing_assessment x
                           WHERE x.del_flag = 0 AND x.assess_type = 4
                        ) t WHERE t.rn = 1
                     ) la WHERE la.admission_id = a.admission_id AND la.risk_level >= 2)
               AND EXISTS (SELECT 1 FROM biz_vte_prevent v
                            WHERE v.del_flag = 0 AND v.admission_id = a.admission_id AND v.execute_status = 1)
            """)
    long countPreventDone(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("deptId") Long deptId);

    /**
     * 院内新发 VTE 患者数（按人算，不是按例次：一人两次 DVT 只算一例）。
     * 只数 event_type IN (1,2) 且 onset_type=1 —— 入院带入(2) 与预防相关出血(3) 都不算。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.discharge_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
               AND EXISTS (SELECT 1 FROM biz_vte_event e
                            WHERE e.del_flag = 0 AND e.admission_id = a.admission_id
                              AND e.event_type IN (1, 2) AND e.onset_type = 1)
            """)
    long countVteEvent(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("deptId") Long deptId);

    /**
     * 预防相关出血患者数（提示性指标，不计入 VTE 发生率）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.discharge_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
               AND EXISTS (SELECT 1 FROM biz_vte_event e
                            WHERE e.del_flag = 0 AND e.admission_id = a.admission_id AND e.event_type = 3)
            """)
    long countBleed(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("deptId") Long deptId);

    /**
     * 按科室生成快照时枚举有出院患者的科室
     */
    @Select("""
            SELECT a.dept_id AS dept_id, MAX(d.dept_name) AS dept_name, COUNT(*) AS patient_count
              FROM biz_admission a
              LEFT JOIN sys_department d ON d.id = a.dept_id
             WHERE a.del_flag = 0 AND a.discharge_time BETWEEN #{from} AND #{to}
               AND a.dept_id IS NOT NULL
             GROUP BY a.dept_id
             ORDER BY COUNT(*) DESC, a.dept_id
            """)
    List<DeptCountRowVO> selectDischargeDepts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * 科室名（快照用；科室是 his-system 的表，裸 SQL 取，不建反向依赖）
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    // 看板（在院视角："今天该干什么"）

    @Select("SELECT COUNT(*) FROM biz_admission a WHERE a.del_flag = 0 AND a.admit_status = 1")
    long countInHospital();

    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0 AND a.admit_status = 1
               AND EXISTS (SELECT 1 FROM biz_nursing_assessment x
                            WHERE x.del_flag = 0 AND x.assess_type = 4 AND x.admission_id = a.admission_id)
            """)
    long countInHospitalAssessed();

    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0 AND a.admit_status = 1
               AND EXISTS (SELECT 1 FROM (
                        SELECT t.* FROM (
                          SELECT x.*, ROW_NUMBER() OVER (PARTITION BY x.admission_id
                                 ORDER BY x.assess_time DESC, x.id DESC) rn
                            FROM biz_nursing_assessment x
                           WHERE x.del_flag = 0 AND x.assess_type = 4
                        ) t WHERE t.rn = 1
                     ) la WHERE la.admission_id = a.admission_id AND la.risk_level >= 2)
            """)
    long countInHospitalHighRisk();

    /**
     * 在院中高危且一条措施都没落实的人数（今天要干的事）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0 AND a.admit_status = 1
               AND EXISTS (SELECT 1 FROM (
                        SELECT t.* FROM (
                          SELECT x.*, ROW_NUMBER() OVER (PARTITION BY x.admission_id
                                 ORDER BY x.assess_time DESC, x.id DESC) rn
                            FROM biz_nursing_assessment x
                           WHERE x.del_flag = 0 AND x.assess_type = 4
                        ) t WHERE t.rn = 1
                     ) la WHERE la.admission_id = a.admission_id AND la.risk_level >= 2)
               AND NOT EXISTS (SELECT 1 FROM biz_vte_prevent v
                                WHERE v.del_flag = 0 AND v.admission_id = a.admission_id AND v.execute_status = 1)
            """)
    long countHighRiskPending();

    /**
     * 自然月内院内新发 VTE 患者数（按确诊日期归月）
     */
    @Select("""
            SELECT COUNT(DISTINCT e.admission_id) FROM biz_vte_event e
             WHERE e.del_flag = 0 AND e.event_type IN (1, 2) AND e.onset_type = 1
               AND e.diagnose_date BETWEEN #{from} AND #{to}
            """)
    long countVteEventByDiagnoseDate(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Select("""
            SELECT COUNT(DISTINCT e.admission_id) FROM biz_vte_event e
             WHERE e.del_flag = 0 AND e.event_type = 3 AND e.diagnose_date BETWEEN #{from} AND #{to}
            """)
    long countBleedByDiagnoseDate(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
