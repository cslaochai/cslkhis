package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.entity.BizStaffDemand;
import com.his.system.vo.StaffDemandGapVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 人力需求 Mapper。
 */
@Mapper
public interface BizStaffDemandMapper extends BaseMapper<BizStaffDemand> {

    /**
     * 派生号段的起点
     */
    @Select("SELECT COALESCE(MAX(id), 896800000000000000) FROM biz_staff_demand "
            + "WHERE id < 896800000000900000")
    Long maxDerivedId();

    /**
     * 缺口清单
     */
    @Select("""
            <script>
            SELECT g.demand_date AS demandDate, g.org_type AS orgType, g.org_id AS orgId,
                   g.org_name AS orgName, g.staff_type AS staffType,
                   g.required_count AS requiredCount, g.scheduled_count AS scheduledCount,
                   g.gap_count AS gapCount, g.demand_source AS demandSource, g.calc_basis AS calcBasis
              FROM v_staff_demand_gap g
             WHERE g.demand_date BETWEEN #{startDate} AND #{endDate}
            <if test="orgType != null"> AND g.org_type = #{orgType}</if>
            <if test="orgId != null">   AND g.org_id = #{orgId}</if>
            <if test="staffType != null"> AND g.staff_type = #{staffType}</if>
             ORDER BY g.demand_date, g.org_type, g.staff_type, g.org_name
            </script>
            """)
    List<StaffDemandGapVO> selectGap(@Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     @Param("orgType") Integer orgType,
                                     @Param("orgId") Long orgId,
                                     @Param("staffType") Integer staffType);

    /**
     * 重算前清掉窗口内的派生产物（手工调整 demand_source=3 一行不动）
     */
    @Delete("DELETE FROM biz_staff_demand "
            + "WHERE demand_source IN (1,2) AND demand_date BETWEEN #{startDate} AND #{endDate}")
    int purgeDerived(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 派生①：住院护理需求 = MAX(Σ(在院患者 × 护理等级工时) ÷ 8h, 病区核定下限, 1)。
     *
     */
    @Insert("""
            INSERT INTO biz_staff_demand (id, demand_date, org_type, org_id, org_name, period_code,
                                          shift_id, staff_type, required_count, demand_source,
                                          source_biz_id, calc_basis, status,
                                          create_by, create_time, del_flag, remark)
            WITH RECURSIVE cal(dt) AS (
              SELECT #{startDate}
              UNION ALL
              SELECT dt + INTERVAL 1 DAY FROM cal WHERE dt < #{endDate}
            )
            SELECT #{idBase} + ROW_NUMBER() OVER (ORDER BY x.dt, x.ward_id),
                   x.dt, 2, x.ward_id, x.ward_name, 0, 0, 2,
                   GREATEST(x.derived, x.floor_cnt, 1), 2, x.ward_id,
                   CONCAT('在院 ', x.patients, ' 人 × 等级工时（特级6.0/一级3.5/二级1.7/三级0.6 h）= ',
                          x.hours, 'h ÷ 8h = ', x.derived,
                          ' 人；病区核定护理下限 ', x.floor_cnt,
                          ' 人（三班覆盖的最低运营配置，与患者数无关）→ 取 MAX = ',
                          GREATEST(x.derived, x.floor_cnt, 1), ' 人'),
                   1, #{operator}, NOW(), 0, #{remark}
              FROM (
                SELECT c.dt AS dt, w.ward_id AS ward_id, MAX(w.ward_name) AS ward_name,
                       COUNT(a.admission_id) AS patients,
                       IFNULL(ROUND(SUM(CASE a.nursing_level WHEN 1 THEN 6.0 WHEN 2 THEN 3.5
                                                             WHEN 3 THEN 1.7 WHEN 4 THEN 0.6 ELSE 1.7 END), 1), 0) AS hours,
                       IFNULL(CEIL(SUM(CASE a.nursing_level WHEN 1 THEN 6.0 WHEN 2 THEN 3.5
                                                            WHEN 3 THEN 1.7 WHEN 4 THEN 0.6 ELSE 1.7 END) / 8), 0) AS derived,
                       IFNULL(MAX(r.min_staff), 0) AS floor_cnt
                  FROM cal c
                  JOIN sys_ward w ON w.status = 1
                  LEFT JOIN biz_admission a
                         ON a.del_flag = 0 AND a.admit_status = 1 AND a.ward_id = w.ward_id
                  LEFT JOIN biz_staff_plan_rule r
                         ON r.del_flag = 0 AND r.status = 1 AND r.org_type = 2
                        AND r.org_id = w.ward_id AND r.staff_type = 2 AND r.shift_id = 0
                 GROUP BY c.dt, w.ward_id
              ) x
            """)
    int deriveInpatient(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                        @Param("idBase") Long idBase, @Param("operator") String operator,
                        @Param("remark") String remark);

    /**
     * 派生②：门诊护理需求 = MAX(分诊 1 人 + 跟诊 CEIL(出诊医生数/2), 科室核定下限, 1)。
     */
    @Insert("""
            INSERT INTO biz_staff_demand (id, demand_date, org_type, org_id, org_name, period_code,
                                          shift_id, staff_type, required_count, demand_source,
                                          source_biz_id, calc_basis, status,
                                          create_by, create_time, del_flag, remark)
            SELECT #{idBase} + ROW_NUMBER() OVER (ORDER BY x.dt, x.dept_id),
                   x.dt, 1, x.dept_id, x.dept_name, 0, 0, 2,
                   GREATEST(x.derived, x.floor_cnt, 1), 1, x.dept_id,
                   CONCAT('出诊医生 ', x.doctors, ' 人：分诊 1 + 跟诊 CEIL(', x.doctors, '/2) = ',
                          x.derived, ' 人；科室核定护理下限 ', x.floor_cnt, ' 人 → 取 MAX = ',
                          GREATEST(x.derived, x.floor_cnt, 1), ' 人'),
                   1, #{operator}, NOW(), 0, #{remark}
              FROM (
                SELECT s.schedule_date AS dt, s.dept_id AS dept_id, MAX(s.dept_name) AS dept_name,
                       COUNT(DISTINCT s.doctor_id) AS doctors,
                       1 + CEIL(COUNT(DISTINCT s.doctor_id) / 2) AS derived,
                       IFNULL(MAX(r.min_staff), 0) AS floor_cnt
                  FROM biz_schedule s
                  LEFT JOIN biz_staff_plan_rule r
                         ON r.del_flag = 0 AND r.status = 1 AND r.org_type = 1
                        AND r.org_id = s.dept_id AND r.staff_type = 2 AND r.shift_id = 0
                 WHERE s.del_flag = 0 AND s.status = 1
                   AND s.schedule_date BETWEEN #{startDate} AND #{endDate}
                   AND EXISTS (SELECT 1 FROM sys_employee e
                                 JOIN sys_employee_post p ON p.employee_id = e.id
                                 JOIN sys_role r2 ON r2.id = p.role_id
                                WHERE e.del_flag = 0 AND e.status = 1 AND e.dept_id = s.dept_id
                                  AND r2.del_flag = 0 AND r2.status = 1 AND r2.staff_type = 2)
                 GROUP BY s.schedule_date, s.dept_id
              ) x
            """)
    int deriveClinicNurse(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                          @Param("idBase") Long idBase, @Param("operator") String operator,
                          @Param("remark") String remark);

    /**
     * 派生③：门诊医生需求 = 当日出诊医生数。
     */
    @Insert("""
            INSERT INTO biz_staff_demand (id, demand_date, org_type, org_id, org_name, period_code,
                                          shift_id, staff_type, required_count, demand_source,
                                          source_biz_id, calc_basis, status,
                                          create_by, create_time, del_flag, remark)
            SELECT #{idBase} + ROW_NUMBER() OVER (ORDER BY x.dt, x.dept_id),
                   x.dt, 1, x.dept_id, x.dept_name, 0, 0, 1,
                   GREATEST(x.doctors, 1), 1, x.dept_id,
                   CONCAT('出诊计划 ', x.plans, ' 条，医生 ', x.doctors, ' 人：需求 = 出诊医生数'),
                   1, #{operator}, NOW(), 0, #{remark}
              FROM (
                SELECT s.schedule_date AS dt, s.dept_id AS dept_id, MAX(s.dept_name) AS dept_name,
                       COUNT(*) AS plans, COUNT(DISTINCT s.doctor_id) AS doctors
                  FROM biz_schedule s
                 WHERE s.del_flag = 0 AND s.status = 1
                   AND s.schedule_date BETWEEN #{startDate} AND #{endDate}
                 GROUP BY s.schedule_date, s.dept_id
              ) x
            """)
    int deriveClinicDoctor(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                           @Param("idBase") Long idBase, @Param("operator") String operator,
                           @Param("remark") String remark);

    /**
     * 手工调整（护士长拍板）：写 demand_source=3，并保留系统算出来的原值在依据里。
     */
    @Insert("""
            INSERT INTO biz_staff_demand (id, demand_date, org_type, org_id, org_name, period_code,
                                          shift_id, staff_type, required_count, demand_source,
                                          source_biz_id, calc_basis, status,
                                          create_by, create_time, update_by, update_time, del_flag, remark)
            VALUES (#{id}, #{demandDate}, #{orgType}, #{orgId}, #{orgName}, 0, 0, #{staffType},
                    #{requiredCount}, 3, #{orgId}, #{calcBasis}, 1,
                    #{operator}, NOW(), #{operator}, NOW(), 0, #{remark})
            ON DUPLICATE KEY UPDATE
                    required_count = #{requiredCount}, demand_source = 3,
                    calc_basis = #{calcBasis}, remark = #{remark},
                    update_by = #{operator}, update_time = NOW()
            """)
    int upsertManual(@Param("id") Long id, @Param("demandDate") LocalDate demandDate,
                     @Param("orgType") Integer orgType, @Param("orgId") Long orgId,
                     @Param("orgName") String orgName, @Param("staffType") Integer staffType,
                     @Param("requiredCount") Integer requiredCount,
                     @Param("calcBasis") String calcBasis, @Param("operator") String operator,
                     @Param("remark") String remark);

    /**
     * 手工调整前先看一眼系统原来算了多少（写进依据里，下次重算回来时有个参照）
     */
    @Select("SELECT required_count FROM biz_staff_demand "
            + "WHERE del_flag = 0 AND demand_date = #{demandDate} AND org_type = #{orgType} "
            + "AND org_id = #{orgId} AND staff_type = #{staffType} AND period_code = 0 AND shift_id = 0")
    Integer selectRequired(@Param("demandDate") LocalDate demandDate, @Param("orgType") Integer orgType,
                           @Param("orgId") Long orgId, @Param("staffType") Integer staffType);

    /**
     * 单元名称快照：手工调整那行要知道自己挂在哪个病区/科室上
     */
    @Select("SELECT dept_name FROM sys_department d WHERE d.id = #{orgId}")
    String selectDeptName(@Param("orgId") Long orgId);

    @Select("SELECT ward_name FROM sys_ward w WHERE w.ward_id = #{orgId} AND w.status = 1")
    String selectWardName(@Param("orgId") Long orgId);
}
