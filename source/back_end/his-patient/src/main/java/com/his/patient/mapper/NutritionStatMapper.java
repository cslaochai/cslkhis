package com.his.patient.mapper;

import com.his.patient.vo.DeptStatRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 营养膳食指标复算（全部裸 SQL）。
 *
 * <p><b>为什么全是裸 SQL</b>：指标跨入院记录 / 营养风险筛查记录 / 膳食方案 /
 * 住院订餐配送 / 会诊申请记录（his-patient）与科室（his-system），
 * 而且要取"每次住院<b>最新一次</b> NRS2002"这种窗口语义 —— 走实体关联会把分页、逻辑删除、
 * 科室快照搅在一起。裸 SQL 把口径摊在明面上，改口径只改这一处。
 *
 * <p><b>分母口径</b>：同期出院患者（出院时间落在统计月内），与 VTE 监测 sql/167、
 * 抗菌药物监测 sql/161 的"按出院归月"保持一致 —— 全院两套分母对不上账是评审最常被抓的点。
 */
@Mapper
public interface NutritionStatMapper {

    /** 同期出院患者数（筛查率/会诊率的分母） */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.discharge_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
            """)
    long countDischarge(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                        @Param("deptId") Long deptId);

    /** 出院患者中做过 NRS2002 筛查的人数（筛查率分子） */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.discharge_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
               AND EXISTS (SELECT 1 FROM biz_nutrition_screen s
                            WHERE s.del_flag = 0 AND s.screen_type = 1 AND s.admission_id = a.admission_id)
            """)
    long countScreened(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                       @Param("deptId") Long deptId);

    /** 出院患者中最新一次 NRS2002 判为有营养风险（总分≥3）的人数 */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0
               AND a.discharge_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR a.dept_id = #{deptId})
               AND EXISTS (SELECT 1 FROM (
                        SELECT t.admission_id, t.total_score,
                               ROW_NUMBER() OVER (PARTITION BY t.admission_id
                                 ORDER BY t.screen_time DESC, t.id DESC) rn
                          FROM biz_nutrition_screen t
                         WHERE t.del_flag = 0 AND t.screen_type = 1
                     ) ls WHERE ls.admission_id = a.admission_id AND ls.rn = 1 AND ls.total_score >= 3)
            """)
    long countRisk(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                   @Param("deptId") Long deptId);

    /** 统计期内开始执行的膳食方案数 */
    @Select("""
            SELECT COUNT(*) FROM biz_diet_plan v
             WHERE v.del_flag = 0
               AND v.start_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR v.dept_id = #{deptId})
            """)
    long countDietPlan(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                       @Param("deptId") Long deptId);

    /** 其中营养科已接收的方案数（膳食医嘱执行率分子） */
    @Select("""
            SELECT COUNT(*) FROM biz_diet_plan v
             WHERE v.del_flag = 0
               AND v.start_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR v.dept_id = #{deptId})
               AND v.confirm_status = 1
            """)
    long countDietConfirm(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                          @Param("deptId") Long deptId);

    /** 统计期内申请的营养会诊数（consult_category=2，按申请科室归口） */
    @Select("""
            SELECT COUNT(*) FROM biz_consultation c
             WHERE c.del_flag = 0
               AND c.consult_category = 2
               AND c.apply_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR c.from_dept_id = #{deptId})
            """)
    long countConsult(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                      @Param("deptId") Long deptId);

    /** 按时应答的营养会诊数（急≤10 分钟、普通≤24 小时；未应答不计） */
    @Select("""
            SELECT COUNT(*) FROM biz_consultation c
             WHERE c.del_flag = 0
               AND c.consult_category = 2
               AND c.apply_time BETWEEN #{from} AND #{to}
               AND (#{deptId} IS NULL OR c.from_dept_id = #{deptId})
               AND c.accept_time IS NOT NULL
               AND TIMESTAMPDIFF(MINUTE, c.apply_time, c.accept_time) <= IF(c.is_urgent = 1, 10, 1440)
            """)
    long countConsultOnTime(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                            @Param("deptId") Long deptId);

    /** 统计期内订餐明细数（不含已取消，签收率分母） */
    @Select("""
            SELECT COUNT(*) FROM biz_meal_order m
             WHERE m.del_flag = 0
               AND m.deliver_status <> 4
               AND m.meal_date BETWEEN #{fromDate} AND #{toDate}
               AND (#{deptId} IS NULL OR m.dept_id = #{deptId})
            """)
    long countMeal(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
                   @Param("deptId") Long deptId);

    /** 已签收的订餐明细数（签收率分子） */
    @Select("""
            SELECT COUNT(*) FROM biz_meal_order m
             WHERE m.del_flag = 0
               AND m.deliver_status = 3
               AND m.meal_date BETWEEN #{fromDate} AND #{toDate}
               AND (#{deptId} IS NULL OR m.dept_id = #{deptId})
            """)
    long countMealSigned(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
                         @Param("deptId") Long deptId);

    /** 退订明细数（提示性指标，不进签收率分母） */
    @Select("""
            SELECT COUNT(*) FROM biz_meal_order m
             WHERE m.del_flag = 0
               AND m.deliver_status = 4
               AND m.meal_date BETWEEN #{fromDate} AND #{toDate}
               AND (#{deptId} IS NULL OR m.dept_id = #{deptId})
            """)
    long countMealCancel(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
                         @Param("deptId") Long deptId);

    /** 按科室生成快照时枚举有出院患者的科室 */
    @Select("""
            SELECT a.dept_id AS dept_id, MAX(d.dept_name) AS dept_name, COUNT(*) AS patient_count
              FROM biz_admission a
              LEFT JOIN sys_department d ON d.id = a.dept_id
             WHERE a.del_flag = 0 AND a.discharge_time BETWEEN #{from} AND #{to}
               AND a.dept_id IS NOT NULL
             GROUP BY a.dept_id
             ORDER BY COUNT(*) DESC, a.dept_id
            """)
    List<DeptStatRowVO> selectDischargeDepts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** 科室名（科室是 his-system 的表，裸 SQL 取，不建反向依赖） */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    // 看板（在院视角："今天该干什么"）

    @Select("SELECT COUNT(*) FROM biz_admission a WHERE a.del_flag = 0 AND a.admit_status = 1")
    long countInHospital();

    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0 AND a.admit_status = 1
               AND EXISTS (SELECT 1 FROM biz_nutrition_screen s
                            WHERE s.del_flag = 0 AND s.screen_type = 1 AND s.admission_id = a.admission_id)
            """)
    long countInHospitalScreened();

    /** 在院中最新一次 NRS2002 有营养风险（总分≥3）的人数 */
    @Select("""
            SELECT COUNT(*) FROM biz_admission a
             WHERE a.del_flag = 0 AND a.admit_status = 1
               AND EXISTS (SELECT 1 FROM (
                        SELECT t.admission_id, t.total_score,
                               ROW_NUMBER() OVER (PARTITION BY t.admission_id
                                 ORDER BY t.screen_time DESC, t.id DESC) rn
                          FROM biz_nutrition_screen t
                         WHERE t.del_flag = 0 AND t.screen_type = 1
                     ) ls WHERE ls.admission_id = a.admission_id AND ls.rn = 1 AND ls.total_score >= 3)
            """)
    long countInHospitalRisk();

    /** 到期未复筛人数（留了下次筛查日期且已到/已过，且仍在院） */
    @Select("""
            SELECT COUNT(DISTINCT a.admission_id) FROM biz_admission a
              JOIN biz_nutrition_screen s ON s.admission_id = a.admission_id
             WHERE a.del_flag = 0 AND a.admit_status = 1
               AND s.del_flag = 0 AND s.next_screen_date IS NOT NULL
               AND s.next_screen_date <= CURDATE()
            """)
    long countReScreenDue();

    /** 执行中但营养科还没接收的膳食方案数 */
    @Select("""
            SELECT COUNT(*) FROM biz_diet_plan v
             WHERE v.del_flag = 0 AND v.plan_status = 1 AND v.confirm_status = 0
            """)
    long countPendingConfirmPlan();

    @Select("""
            SELECT COUNT(*) FROM biz_meal_order m
             WHERE m.del_flag = 0 AND m.meal_date = #{mealDate} AND m.deliver_status <> 4
            """)
    long countMealOfDay(@Param("mealDate") LocalDate mealDate);

    @Select("""
            SELECT COUNT(*) FROM biz_meal_order m
             WHERE m.del_flag = 0 AND m.meal_date = #{mealDate} AND m.deliver_status = 3
            """)
    long countMealSignedOfDay(@Param("mealDate") LocalDate mealDate);

    @Select("""
            SELECT COUNT(*) FROM biz_meal_order m
             WHERE m.del_flag = 0 AND m.meal_date = #{mealDate} AND m.deliver_status IN (0, 1)
            """)
    long countMealPendingOfDay(@Param("mealDate") LocalDate mealDate);

    /** 营养会诊未完成数（待应答 + 已应答） */
    @Select("""
            SELECT COUNT(*) FROM biz_consultation c
             WHERE c.del_flag = 0 AND c.consult_category = 2 AND c.consult_status IN (0, 3)
            """)
    long countConsultUnfinished();

    /** 营养会诊超时未应答数（待应答且已过时限：急 10 分钟、普通 24 小时） */
    @Select("""
            SELECT COUNT(*) FROM biz_consultation c
             WHERE c.del_flag = 0 AND c.consult_category = 2 AND c.consult_status = 0
               AND ((c.is_urgent = 1 AND c.apply_time < NOW() - INTERVAL 10 MINUTE)
                    OR (c.is_urgent = 0 AND c.apply_time < NOW() - INTERVAL 24 HOUR))
            """)
    long countConsultOverdue();
}
