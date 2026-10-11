package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.vo.NurseScheduleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 护理排班 Mapper（sql/166 起；2026-10-11 合并进排班底座后无独立护理表）。
 *
 * <p>护理格 = {@code biz_schedule} 里 {@code staff_type IN (2,7)} 且落在护理单元上的行：
 * 读写全部走底座，本接口只负责「单元/在册护士/护理班次」三类参照数据
 * 与护理视图的取数（矩阵格子、台账、月度工时）。
 */
@Mapper
public interface NurseScheduleMapper {

    /**
     * 在册护理口径：在岗未删，且<b>岗位角色上的岗位类别属于护理序列</b>。
     *
     * <p>含 {@code StaffTypeEnum.NURSE(2) 护理} 与 {@code StaffTypeEnum.NURSING_ASSISTANT(7) 护工/护理辅助}：
     * 护工没有执业资格、不占护理人力配编，但和护士同在病区干活、同样要被排班，
     * 所以「护理排班能排到谁」这一处口径对两类人共用。
     *
     * <p>走 {@code sys_role.staff_type}（人 → 岗位类别），不是员工档案上那个可能不同步的 emp_type。
     * 这里必须是<b>编译期字符串常量</b>——它要被拼进 {@code @Select} 注解，换成枚举方法调用会直接编译不过，
     * 所以码值写成字面量，跟着 StaffTypeEnum 的注释走。
     */
    String NURSE_WHERE = " e.del_flag = 0 AND e.status = 1 "
            + " AND EXISTS (SELECT 1 FROM sys_employee_post p "
            + "               JOIN sys_role r ON r.id = p.role_id "
            + "              WHERE p.employee_id = e.id AND r.del_flag = 0 AND r.status = 1 "
            + "                AND r.staff_type IN (2, 7)) ";

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
     * 矩阵格子：区间内本单元全部行（休息/请假也渲染，否则看不出「这人这天的走向」）。
     *
     * <p>护理格就是底座 {@code biz_schedule} 里 {@code staff_type IN (2,7)} 的行（护理排班已并入底座），
     * 班次名不冗余存储，从班次字典现 JOIN——字典改名不该让历史格子带着旧名。
     * {@code shift_id} 用 {@code NULLIF(_, 0)} 洗成 NULL：底座用 0 表示「无班次」（休息行），
     * 护理页的口径是 NULL，混着会让「非上班却挂班次」的告警误报。
     */
    @Select("""
            SELECT s.id, s.employee_id AS employeeId,
                   DATE_FORMAT(s.schedule_date, '%Y-%m-%d') AS scheduleDate,
                   s.week_day AS weekDay, NULLIF(s.shift_id, 0) AS shiftId, f.shift_name AS shiftName,
                   s.start_time AS startTime, s.end_time AS endTime, s.work_minutes AS workMinutes,
                   s.duty_status AS scheduleStatus, s.schedule_source AS scheduleSource, s.remark
              FROM biz_schedule s
              LEFT JOIN biz_shift f ON f.id = s.shift_id
             WHERE s.del_flag = 0 AND s.staff_type IN (2, 7)
               AND s.org_type = #{orgType} AND s.org_id = #{orgId}
               AND s.schedule_date BETWEEN #{startDate} AND #{endDate}
             ORDER BY s.schedule_date, s.employee_id, s.start_time
            """)
    List<NurseScheduleVO.Cell> selectCells(@Param("orgType") Integer orgType,
                                           @Param("orgId") Long orgId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 某护士在一段日期内的合计工时（分钟）：单周工时上限判定的取数口径。
     * 只算护理序列（2/7）的行——这个人若另有医生等身份的排班，不该混进护理工时。
     */
    @Select("""
            SELECT COALESCE(SUM(work_minutes), 0)
              FROM biz_schedule
             WHERE del_flag = 0 AND staff_type IN (2, 7)
               AND employee_id = #{employeeId}
               AND duty_status = 1
               AND schedule_date BETWEEN #{startDate} AND #{endDate}
            """)
    int selectWorkMinutes(@Param("employeeId") Long employeeId,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);

    /**
     * 月度工时：一人一行。
     *
     * <p>底座允许同人同日多条（多班次），所以先按「人 × 日」归并成天粒度再汇总：
     * 一天只要有一条上班行就计 1 个上班天（不因早晚双班计 2 天），夜班天/工时同理按天归并。
     * 夜班口径按班次<b>开始时刻</b>判（&lt;08:00 或 &ge;16:00），与「连续夜班」同一条口径，
     * 不依赖班次名里有没有「夜」字。职称从员工表现取——底座不冗余职称列。
     */
    @Select("""
            SELECT t.employee_id AS employeeId, MAX(t.emp_code) AS empCode, MAX(t.employee_name) AS nurseName,
                   MAX(e.title) AS nurseTitle,
                   SUM(t.is_work) AS workDays,
                   SUM(t.is_rest) AS restDays,
                   SUM(t.is_leave) AS leaveDays,
                   SUM(t.is_training) AS trainingDays,
                   SUM(t.is_suspended) AS suspendedDays,
                   SUM(t.is_night) AS nightDays,
                   SUM(t.minutes) AS workMinutes,
                   ROUND(SUM(t.minutes) / 60, 1) AS workHours,
                   COUNT(*) AS writtenDays
              FROM (
                SELECT employee_id, MAX(emp_code) AS emp_code, MAX(employee_name) AS employee_name, schedule_date,
                       MAX(CASE WHEN duty_status = 1 THEN 1 ELSE 0 END) AS is_work,
                       MAX(CASE WHEN duty_status = 2 THEN 1 ELSE 0 END) AS is_rest,
                       MAX(CASE WHEN duty_status = 3 THEN 1 ELSE 0 END) AS is_leave,
                       MAX(CASE WHEN duty_status = 4 THEN 1 ELSE 0 END) AS is_training,
                       MAX(CASE WHEN duty_status = 5 THEN 1 ELSE 0 END) AS is_suspended,
                       MAX(CASE WHEN duty_status = 1 AND (start_time < '08:00' OR start_time >= '16:00')
                                THEN 1 ELSE 0 END) AS is_night,
                       COALESCE(SUM(CASE WHEN duty_status = 1 THEN work_minutes ELSE 0 END), 0) AS minutes
                  FROM biz_schedule
                 WHERE del_flag = 0 AND staff_type IN (2, 7)
                   AND org_type = #{orgType} AND org_id = #{orgId}
                   AND schedule_date BETWEEN #{startDate} AND #{endDate}
                 GROUP BY employee_id, schedule_date
              ) t
              LEFT JOIN sys_employee e ON e.id = t.employee_id
             GROUP BY t.employee_id
             ORDER BY MAX(t.emp_code), t.employee_id
            """)
    List<NurseScheduleVO.Workload> selectWorkload(@Param("orgType") Integer orgType,
                                                  @Param("orgId") Long orgId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * 排班台账分页（护理部跨病区回看）。班次名/职称字典与员工表现取，不依赖已删除的护理副本表。
     */
    @Select("""
            <script>
            SELECT l.id, l.org_id AS wardId, l.org_name AS wardName, l.dept_id AS deptId, l.dept_name AS deptName,
                   DATE_FORMAT(l.schedule_date, '%Y-%m-%d') AS scheduleDate, l.week_day AS weekDay,
                   l.employee_id AS employeeId, l.emp_code AS empCode, l.employee_name AS nurseName,
                   e.title AS nurseTitle, NULLIF(l.shift_id, 0) AS shiftId, f.shift_name AS shiftName,
                   l.start_time AS startTime, l.end_time AS endTime, l.work_minutes AS workMinutes,
                   l.duty_status AS scheduleStatus, l.schedule_source AS scheduleSource, l.remark
              FROM biz_schedule l
              LEFT JOIN biz_shift f ON f.id = l.shift_id
              LEFT JOIN sys_employee e ON e.id = l.employee_id
             WHERE l.del_flag = 0 AND l.staff_type IN (2, 7)
            <if test="keyword != null and keyword != ''">
              AND (l.employee_name LIKE CONCAT('%', #{keyword}, '%')
                OR l.emp_code LIKE CONCAT('%', #{keyword}, '%')
                OR l.org_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="wardId != null"> AND l.org_id = #{wardId}</if>
            <if test="deptId != null"> AND l.dept_id = #{deptId}</if>
            <if test="scheduleStatus != null"> AND l.duty_status = #{scheduleStatus}</if>
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
     * 点格定位：本单元这个护理这一天的那条格（一人一天一格是本页的语义；
     * 若「全院排班」给同一人同日排了多班，取最早班为主格，改格时按覆盖语义收成一条）。
     */
    @Select("""
            SELECT id, employee_id AS employeeId, DATE_FORMAT(schedule_date, '%Y-%m-%d') AS scheduleDate,
                   NULLIF(shift_id, 0) AS shiftId, work_minutes AS workMinutes, duty_status AS scheduleStatus
              FROM biz_schedule
             WHERE del_flag = 0 AND staff_type IN (2, 7)
               AND employee_id = #{employeeId} AND schedule_date = #{scheduleDate}
               AND org_type = #{orgType} AND org_id = #{orgId}
             ORDER BY start_time
             LIMIT 1
            """)
    NurseScheduleVO.Cell selectUnitCell(@Param("employeeId") Long employeeId,
                                        @Param("scheduleDate") LocalDate scheduleDate,
                                        @Param("orgType") Integer orgType,
                                        @Param("orgId") Long orgId);
}
