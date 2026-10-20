package com.his.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.system.dto.DutyCheckRow;
import com.his.system.entity.BizDutyRoster;
import com.his.system.vo.DutyWorkloadVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 全院总值班排班 Mapper
 */
@Mapper
public interface BizDutyRosterMapper extends BaseMapper<BizDutyRoster> {

    /**
     * 物理删除。
     *
     * <p>为什么不用 {@code deleteById}：全院总值班排班的唯一键
     * {@code uk_duty_date_shift_role(duty_date, shift_type, role_type)} <b>不含 del_flag</b>，
     * 而 BaseEntity.delFlag 带 {@code @TableLogic}，软删的行照样占着唯一键 ——
     * 「删掉今天白班主班再重新排一个人」必撞 Duplicate entry。排班是配置、没有留档价值，
     * 所以这里的删除一律是真删（见 AGENTS.md §3）。
     */
    @Delete("DELETE FROM biz_duty_roster WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    /**
     * 区间内全部值班行（按实际值班人解析：换班后按顶替人归并）。
     * 值班页「规则校验」一次捞齐。
     */
    @Select("""
            <script>
            SELECT d.id, d.duty_date AS dutyDate, d.shift_type AS shiftType, d.role_type AS roleType,
                   d.post_id AS postId, p.post_name AS postName,
                   COALESCE(NULLIF(d.substitute_emp_id, 0), d.employee_id) AS employeeId,
                   COALESCE(d.substitute_emp_name, d.employee_name) AS employeeName,
                   d.dept_name AS deptName
              FROM biz_duty_roster d
              LEFT JOIN biz_duty_post p ON p.id = d.post_id
             WHERE d.del_flag = 0 AND d.publish_status IN (2, 4)
            <if test="postId != null"> AND d.post_id = #{postId}</if>
               AND d.duty_date BETWEEN #{startDate} AND #{endDate}
             ORDER BY d.duty_date, d.post_id, d.shift_type, d.role_type, d.id
            </script>
            """)
    List<DutyCheckRow> selectDutyRows(@Param("postId") Long postId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    /**
     * 月度值班工时：一人一行，按实际值班人（换班后按顶替人）归并。
     */
    @Select("""
            <script>
            SELECT d.emp_id AS employeeId, MAX(e.emp_code) AS empCode,
                   MAX(COALESCE(d.employee_name, e.emp_name)) AS employeeName,
                   MAX(d.dept_name) AS deptName,
                   SUM(d.is_day) AS dayCount,
                   SUM(d.is_night) AS nightCount,
                   SUM(d.is_primary) AS primaryCount,
                   SUM(d.is_secondary) AS secondaryCount,
                   SUM(d.minutes) AS totalMinutes,
                   ROUND(SUM(d.minutes) / 60, 1) AS totalHours
              FROM (
                SELECT COALESCE(NULLIF(substitute_emp_id, 0), employee_id) AS emp_id,
                       MAX(employee_name) AS employee_name, MAX(dept_name) AS dept_name,
                       CASE WHEN shift_type = 1 THEN 1 ELSE 0 END AS is_day,
                       CASE WHEN shift_type = 2 THEN 1 ELSE 0 END AS is_night,
                       CASE WHEN role_type = 1 THEN 1 ELSE 0 END AS is_primary,
                       CASE WHEN role_type = 2 THEN 1 ELSE 0 END AS is_secondary,
                       COALESCE(f.duration_minutes, 0) AS minutes
                  FROM biz_duty_roster d
                  LEFT JOIN biz_shift f ON f.id = d.shift_id
                 WHERE d.del_flag = 0 AND d.publish_status IN (2, 4)
                <if test="postId != null"> AND d.post_id = #{postId}</if>
                   AND d.duty_date BETWEEN #{startDate} AND #{endDate}
              ) d
              LEFT JOIN sys_employee e ON e.id = d.emp_id
             GROUP BY d.emp_id
             ORDER BY MAX(e.emp_code), d.emp_id
            </script>
            """)
    List<DutyWorkloadVO.Row> selectDutyWorkload(@Param("postId") Long postId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}
