package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizNurseSchedule;
import com.his.patient.vo.NurseScheduleVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 病区护理排班 Mapper（sql/166）。
 */
@Mapper
public interface BizNurseScheduleMapper extends BaseMapper<BizNurseSchedule> {

    /**
     * 在册护士口径：在岗未删，且<b>岗位角色上的岗位类别是护理</b>。
     */
    String NURSE_WHERE = " e.del_flag = 0 AND e.status = 1 "
            + " AND EXISTS (SELECT 1 FROM sys_employee_post p "
            + "               JOIN sys_role r ON r.id = p.role_id "
            + "              WHERE p.employee_id = e.id AND r.del_flag = 0 AND r.status = 1 "
            + "                AND r.staff_type = 2) ";

    /**
     * 病区候选：只列启用病区；nurseCount=0 的病区排不了班，但要在下拉里看得见原因
     */
    @Select("""
            <script>
            SELECT 1 AS unitType, w.ward_id AS wardId, w.ward_code AS wardCode, w.ward_name AS wardName,
                   w.dept_id AS deptId, d.dept_name AS deptName,
                   (SELECT COUNT(*) FROM sys_employee e WHERE
            """ + NURSE_WHERE + """
             AND e.dept_id = w.dept_id) AS nurseCount
              FROM sys_ward w
              LEFT JOIN sys_department d ON d.id = w.dept_id
             WHERE w.status = 1
            <if test="deptIds != null">
              AND w.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (w.ward_name LIKE CONCAT('%', #{keyword}, '%') OR w.ward_code LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY w.dept_id, w.ward_code, w.ward_id
            </script>
            """)
    List<NurseScheduleVO.Ward> selectWardOptions(@Param("deptIds") List<Long> deptIds,
                                                 @Param("keyword") String keyword);

    /**
     * 病区快照（写排班前必查：病区存在且启用，并用它的 dept 做数据范围与护士归属判定）
     */
    @Select("""
            SELECT 1 AS unitType, w.ward_id AS wardId, w.ward_code AS wardCode, w.ward_name AS wardName,
                   w.dept_id AS deptId, d.dept_name AS deptName, 0 AS nurseCount
              FROM sys_ward w
              LEFT JOIN sys_department d ON d.id = w.dept_id
             WHERE w.ward_id = #{wardId} AND w.status = 1
            """)
    NurseScheduleVO.Ward selectWard(@Param("wardId") Long wardId);

    /**
     * 排班单元快照：unit_type=1 查病区，=2 查门诊科室（sql/209）。
     *
     * <p>两种单元的 id 不在同一个空间，所以必须带着类型一起查 —— 只给 id 会出现
     * 「门诊科室的 id 正好等于某个病区的 id」这种张冠李戴。
     */
    @Select("""
            SELECT #{unitType} AS unitType, x.wardId, x.wardCode, x.wardName, x.deptId, x.deptName, 0 AS nurseCount
              FROM (
                SELECT w.ward_id AS wardId, w.ward_code AS wardCode, w.ward_name AS wardName,
                       w.dept_id AS deptId, d.dept_name AS deptName
                  FROM sys_ward w LEFT JOIN sys_department d ON d.id = w.dept_id
                 WHERE #{unitType} = 1 AND w.ward_id = #{unitId} AND w.status = 1
                UNION ALL
                SELECT t.id, t.dept_code, t.dept_name, t.id, t.dept_name
                  FROM sys_department t
                 WHERE #{unitType} = 2 AND t.id = #{unitId} AND t.del_flag = 0 AND t.status = 1
              ) x
             LIMIT 1
            """)
    NurseScheduleVO.Ward selectUnit(@Param("unitType") Integer unitType, @Param("unitId") Long unitId);

    /**
     * 门诊科室候选：只列「有护理编制」的启用科室（sql/209）。
     *
     * <p>没有护士的门诊科室放进来只会让排班员点进去发现排不了人，这一点跟病区侧同口径。
     * <b>排除有病区的科室</b> —— 那些科室的护理走病区排班，重复入口只会让同一批人在两处被排。
     */
    @Select("""
            <script>
            SELECT 2 AS unitType, t.id AS wardId, t.dept_code AS wardCode, t.dept_name AS wardName,
                   t.id AS deptId, t.dept_name AS deptName,
                   (SELECT COUNT(*) FROM sys_employee e WHERE
            """ + NURSE_WHERE + """
             AND e.dept_id = t.id) AS nurseCount
              FROM sys_department t
             WHERE t.del_flag = 0 AND t.status = 1
               AND NOT EXISTS (SELECT 1 FROM sys_ward w WHERE w.status = 1 AND w.dept_id = t.id)
            <if test="deptIds != null">
              AND t.id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (t.dept_name LIKE CONCAT('%', #{keyword}, '%') OR t.dept_code LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY t.dept_code, t.id
            </script>
            """)
    List<NurseScheduleVO.Ward> selectDeptOptions(@Param("deptIds") List<Long> deptIds,
                                                 @Param("keyword") String keyword);

    /**
     * 护士候选（矩阵的行轴）：按工号定序，行序稳定，复制上周才认得出「同一个人」
     */
    @Select("""
            <script>
            SELECT e.id AS employeeId, e.emp_code AS empCode, e.emp_name AS nurseName,
                   e.title AS nurseTitle, e.dept_id AS deptId, e.dept_name AS deptName
              FROM sys_employee e
             WHERE """ + NURSE_WHERE + """
            <if test="deptId != null"> AND e.dept_id = #{deptId}</if>
            <if test="keyword != null and keyword != ''">
              AND (e.emp_name LIKE CONCAT('%', #{keyword}, '%') OR e.emp_code LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY e.emp_code, e.id
             LIMIT #{limit}
            </script>
            """)
    List<NurseScheduleVO.Nurse> selectNurses(@Param("deptId") Long deptId,
                                             @Param("keyword") String keyword,
                                             @Param("limit") int limit);

    /**
     * 单个在册护士快照；不是护理岗/已停用直接查不出来（返回 null 由服务层报错）。
     * 故意把合法性写在 SQL 里：判据只有一处口径，不会出现「Service 用 A 条件选出来、写入时 B 条件不认」。
     */
    @Select("""
            SELECT e.id AS employeeId, e.emp_code AS empCode, e.emp_name AS nurseName,
                   e.title AS nurseTitle, e.dept_id AS deptId, e.dept_name AS deptName
              FROM sys_employee e
             WHERE """ + NURSE_WHERE + """
             AND e.id = #{employeeId}
            """)
    NurseScheduleVO.Nurse selectNurse(@Param("employeeId") Long employeeId);

    /**
     * 护理班次（use_scope=2 且启用）：矩阵格子唯一可选的班次来源
     */
    @Select("""
            SELECT s.id AS shiftId, s.shift_name AS shiftName, s.start_time AS startTime,
                   s.end_time AS endTime, s.duration_minutes AS durationMinutes
              FROM biz_shift s
             WHERE s.del_flag = 0 AND s.status = 1 AND s.use_scope = 2
             ORDER BY s.start_time, s.id
            """)
    List<NurseScheduleVO.ShiftOption> selectNursingShifts();

    /**
     * 单个可用护理班次（起止时间/时长的唯一来源，前端传什么都不认）
     */
    @Select("""
            SELECT s.id AS shiftId, s.shift_name AS shiftName, s.start_time AS startTime,
                   s.end_time AS endTime, s.duration_minutes AS durationMinutes
              FROM biz_shift s
             WHERE s.del_flag = 0 AND s.status = 1 AND s.use_scope = 2 AND s.id = #{shiftId}
            """)
    NurseScheduleVO.ShiftOption selectNursingShift(@Param("shiftId") Long shiftId);

    /**
     * 矩阵格子：区间内本病区全部行（休息/请假也渲染，否则看不出「这人这天的走向」）
     */
    @Select("""
            SELECT id, employee_id AS employeeId,
                   DATE_FORMAT(schedule_date, '%Y-%m-%d') AS scheduleDate,
                   week_day AS weekDay, shift_id AS shiftId, shift_name AS shiftName,
                   start_time AS startTime, end_time AS endTime, work_minutes AS workMinutes,
                   schedule_status AS scheduleStatus, schedule_source AS scheduleSource, remark
              FROM biz_nurse_schedule
             WHERE del_flag = 0 AND unit_type = #{unitType} AND unit_id = #{unitId}
               AND schedule_date BETWEEN #{startDate} AND #{endDate}
             ORDER BY schedule_date, employee_id
            """)
    List<NurseScheduleVO.Cell> selectCells(@Param("unitType") Integer unitType,
                                           @Param("unitId") Long unitId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 某护士在一段日期内的合计工时（分钟）：单周工时上限判定的取数口径
     */
    @Select("""
            SELECT COALESCE(SUM(work_minutes), 0)
              FROM biz_nurse_schedule
             WHERE del_flag = 0 AND employee_id = #{employeeId}
               AND schedule_status = 1
               AND schedule_date BETWEEN #{startDate} AND #{endDate}
            """)
    int selectWorkMinutes(@Param("employeeId") Long employeeId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);

    /**
     * 月度工时：一人一行。
     * 夜班口径按班次<b>开始时刻</b>判（&lt;08:00 或 &ge;16:00），与「连续夜班」同一条口径，
     * 不依赖班次名里有没有「夜」字（改个名字就不算夜班是不可靠的）。
     * 别名 writtenDays 是「已写行的天数」，缺排天数由服务层用区间天数现算。
     */
    @Select("""
            SELECT employee_id AS employeeId, MAX(emp_code) AS empCode, MAX(nurse_name) AS nurseName,
                   MAX(nurse_title) AS nurseTitle,
                   SUM(schedule_status = 1) AS workDays,
                   SUM(schedule_status = 2) AS restDays,
                   SUM(schedule_status = 3) AS leaveDays,
                   SUM(schedule_status = 4) AS trainingDays,
                   SUM(schedule_status = 5) AS suspendedDays,
                   SUM(schedule_status = 1 AND (start_time < '08:00' OR start_time >= '16:00')) AS nightDays,
                   COALESCE(SUM(work_minutes), 0) AS workMinutes,
                   ROUND(COALESCE(SUM(work_minutes), 0) / 60, 1) AS workHours,
                   COUNT(*) AS writtenDays
              FROM biz_nurse_schedule
             WHERE del_flag = 0 AND unit_type = #{unitType} AND unit_id = #{unitId}
               AND schedule_date BETWEEN #{startDate} AND #{endDate}
             GROUP BY employee_id
             ORDER BY MAX(emp_code), employee_id
            """)
    List<NurseScheduleVO.Workload> selectWorkload(@Param("unitType") Integer unitType,
                                                  @Param("unitId") Long unitId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * 排班台账分页（护理部跨病区回看；列全是快照，不需要 JOIN）
     */
    @Select("""
            <script>
            SELECT l.id, l.ward_id AS wardId, l.ward_name AS wardName, l.dept_id AS deptId, l.dept_name AS deptName,
                   DATE_FORMAT(l.schedule_date, '%Y-%m-%d') AS scheduleDate, l.week_day AS weekDay,
                   l.employee_id AS employeeId, l.emp_code AS empCode, l.nurse_name AS nurseName,
                   l.nurse_title AS nurseTitle, l.shift_id AS shiftId, l.shift_name AS shiftName,
                   l.start_time AS startTime, l.end_time AS endTime, l.work_minutes AS workMinutes,
                   l.schedule_status AS scheduleStatus, l.schedule_source AS scheduleSource, l.remark
              FROM biz_nurse_schedule l
             WHERE l.del_flag = 0
            <if test="keyword != null and keyword != ''">
              AND (l.nurse_name LIKE CONCAT('%', #{keyword}, '%')
                OR l.emp_code LIKE CONCAT('%', #{keyword}, '%')
                OR l.ward_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="wardId != null"> AND l.ward_id = #{wardId}</if>
            <if test="deptId != null"> AND l.dept_id = #{deptId}</if>
            <if test="scheduleStatus != null"> AND l.schedule_status = #{scheduleStatus}</if>
            <if test="startDate != null"> AND l.schedule_date &gt;= #{startDate}</if>
            <if test="endDate != null"> AND l.schedule_date &lt;= #{endDate}</if>
            <if test="deptIds != null">
              AND l.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
            </if>
             ORDER BY l.schedule_date DESC, l.id DESC
            </script>
            """)
    List<NurseScheduleVO.Row> selectSchedulePage(IPage<NurseScheduleVO.Row> page,
                                                 @Param("keyword") String keyword,
                                                 @Param("wardId") Long wardId,
                                                 @Param("deptId") Long deptId,
                                                 @Param("scheduleStatus") Integer scheduleStatus,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("deptIds") List<Long> deptIds);

    /**
     * 点格定位（唯一键 uk_nurse_date 命中，最多一行）
     */
    @Select("""
            SELECT id, employee_id AS employeeId, DATE_FORMAT(schedule_date, '%Y-%m-%d') AS scheduleDate,
                   shift_id AS shiftId, work_minutes AS workMinutes, schedule_status AS scheduleStatus
              FROM biz_nurse_schedule
             WHERE del_flag = 0 AND employee_id = #{employeeId} AND schedule_date = #{scheduleDate}
             LIMIT 1
            """)
    NurseScheduleVO.Cell selectByKey(@Param("employeeId") Long employeeId,
                                     @Param("scheduleDate") LocalDate scheduleDate);

    /**
     * 物理删排班行。
     *
     * <p>唯一键 {@code uk_nurse_date(employee_id, schedule_date)} <b>不含 del_flag</b>：
     * 软删的行仍占着键，「删掉这格再重新排同一个人同一天」会直接 Duplicate entry（AGENTS §3）。
     * 排班行也没有留档价值——改动历史在字段级修改日志里，不在这张表上。
     */
    @Delete("DELETE FROM biz_nurse_schedule WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
