package com.his.outpatient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.outpatient.dto.ErDeptRow;
import com.his.outpatient.dto.NurseDutyRow;
import com.his.outpatient.dto.StaffCheckRow;
import com.his.outpatient.vo.NurseScheduleVO;
import com.his.outpatient.vo.StaffWorkloadVO;
import com.his.outpatient.vo.StaffWorkingGroupVO;
import com.his.outpatient.entity.BizSchedule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工排班（biz_schedule，含护理排班只读查询）
 */
@Mapper
public interface BizScheduleMapper extends BaseMapper<BizSchedule> {

    /**
     * 跨模块只读：取某科室某天在岗（上班）护士。
     *
     * @param deptId 科室 ID（门诊科室或病区所属科室）
     * @param date   排班日期
     */
    @Select("SELECT id, employee_id, employee_name AS nurse_name, dept_id, dept_name, "
            + "NULLIF(shift_id, 0) AS shift_id, start_time, end_time, duty_status AS schedule_status "
            + "FROM biz_schedule "
            + "WHERE del_flag = 0 AND staff_type IN (2, 7) "
            + "AND dept_id = #{deptId} AND schedule_date = #{date} AND duty_status = 1 "
            + "AND publish_status IN (2, 4)")
    List<NurseDutyRow> listOnDuty(@Param("deptId") Long deptId, @Param("date") LocalDate date);

    /**
     * 在册护理口径：在岗未删，且<b>岗位角色上的岗位类别属于护理序列</b>。
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
     * 排班单元快照：unit_type=1 查病区，=2 查门诊科室
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
     * 门诊科室候选：只列「有护理编制」的启用科室
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
     * 急诊科室单元（sys_department.dept_type 含指定类型码，字典 his_dept_type）。
     * <p>跨模块只读主数据，按仓库铁律走本模块裸 SQL，必带 del_flag=0。
     * dept_type 是逗号分隔多值串，匹配用 FIND_IN_SET 而非等值。
     * <p>为什么现查而不是常量/缓存：科室档案上勾「急诊科室」是运行时行为，
     * 现查才能让勾选立即对值班公开表与资质校验生效，也不留换库失效的魔数。
     *
     * @param deptTypeCode 科室类型码（DeptTypeEnum.ER = "6"）
     */
    @Select("""
            SELECT t.id AS id, t.parent_id AS parentId
              FROM sys_department t
             WHERE t.del_flag = 0 AND FIND_IN_SET(#{deptTypeCode}, t.dept_type) > 0
             ORDER BY t.id
            """)
    List<ErDeptRow> selectErDepts(@Param("deptTypeCode") String deptTypeCode);

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
     */
    @Select("""
            SELECT s.id, s.employee_id AS employeeId,
                   DATE_FORMAT(s.schedule_date, '%Y-%m-%d') AS scheduleDate,
                   s.week_day AS weekDay, NULLIF(s.shift_id, 0) AS shiftId, f.shift_name AS shiftName,
                   s.start_time AS startTime, s.end_time AS endTime, s.work_minutes AS workMinutes,
                   s.duty_status AS scheduleStatus, s.publish_status AS publishStatus,
                   s.schedule_source AS scheduleSource, s.remark
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
                   l.duty_status AS scheduleStatus, l.publish_status AS publishStatus,
                   l.schedule_source AS scheduleSource, l.remark
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

    /**
     * 物理删单行。
     */
    @Delete("DELETE FROM biz_schedule WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    /**
     * 物理删掉某个单元里这个人当天的全部事实行（改格/删格用，同样撞键所以不能软删）。
     */
    @Delete("DELETE FROM biz_schedule WHERE org_type = #{orgType} AND org_id = #{orgId} "
            + "AND employee_id = #{employeeId} AND schedule_date = #{scheduleDate} "
            + "AND (#{keepId} IS NULL OR id <> #{keepId})")
    int purgeByDay(@Param("orgType") Integer orgType, @Param("orgId") Long orgId,
                   @Param("employeeId") Long employeeId, @Param("scheduleDate") LocalDate scheduleDate,
                   @Param("keepId") Long keepId);

    /**
     * 在岗人次聚合（日期 × 单元 × 班次 × 岗位）：总览矩阵与人力缺口对比共用一次扫描。
     */
    @Select("SELECT schedule_date AS scheduleDate, org_type AS orgType, org_id AS orgId, "
            + "MAX(org_name) AS orgName, shift_id AS shiftId, staff_type AS staffType, COUNT(*) AS cnt "
            + "FROM biz_schedule "
            + "WHERE del_flag = 0 AND duty_status = 1 AND publish_status IN (2, 4) "
            + "AND schedule_date BETWEEN #{begin} AND #{end} "
            + "GROUP BY schedule_date, org_type, org_id, shift_id, staff_type")
    List<StaffWorkingGroupVO> groupWorkingByUnitShift(@Param("begin") LocalDate begin,
                                                      @Param("end") LocalDate end);

    /**
     * 整周批量迁移发布态：只搬「本单元 + 本周 + 指定原态」的行，不碰已发布/已变更/调班中。
     * 整周提交（草稿0→待审核1）与整周审核发布（待审核1→已发布2）共用这一条 UPDATE。
     *
     * @return 实际迁移行数
     */
    @Update("UPDATE biz_schedule SET publish_status = #{toStatus} "
            + "WHERE del_flag = 0 AND staff_type IN (2, 7) "
            + "AND org_type = #{orgType} AND org_id = #{orgId} "
            + "AND schedule_date BETWEEN #{startDate} AND #{endDate} "
            + "AND publish_status = #{fromStatus}")
    int updateWeekPublishStatus(@Param("orgType") Integer orgType, @Param("orgId") Long orgId,
                                @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                                @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus);

    /**
     * 整周批量迁移发布态（全区岗位通用版）：与 {@link #updateWeekPublishStatus} 的唯一区别是
     * 不过滤 staff_type，覆盖医生/护理/医技/药学/收费/行政等全部岗位类别。
     * 用于「全院岗位排班」整周提交/整周审核发布。
     *
     * <p>员工→门诊联动：门诊绑定行（被 {@code biz_clinic_source} 引用）的发布态由门诊侧
     * {@code syncCoreSchedulePublish} 独占驱动，整周批量不得带上它们，否则与门诊侧互相覆盖。
     *
     * @return 实际迁移行数
     */
    @Update("UPDATE biz_schedule SET publish_status = #{toStatus} "
            + "WHERE del_flag = 0 "
            + "AND org_type = #{orgType} AND org_id = #{orgId} "
            + "AND schedule_date BETWEEN #{startDate} AND #{endDate} "
            + "AND publish_status = #{fromStatus} "
            + "AND NOT EXISTS (SELECT 1 FROM biz_clinic_source cs "
            + "    WHERE cs.del_flag = 0 AND cs.staff_schedule_id = biz_schedule.id)")
    int updateWeekPublishStatusAll(@Param("orgType") Integer orgType, @Param("orgId") Long orgId,
                                   @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                                   @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus);

    /**
     * 区间内全部排班行（全区岗位通用，含班次夜班标记）：
     * 员工排班页「规则校验」一次捞齐，避免按人按日逐条查询。
     */
    @Select("""
            <script>
            SELECT s.id, s.schedule_date AS scheduleDate, s.org_type AS orgType, s.org_id AS orgId,
                   s.org_name AS orgName, s.employee_id AS employeeId, s.emp_code AS empCode,
                   s.employee_name AS employeeName, s.staff_type AS staffType,
                   NULLIF(s.shift_id, 0) AS shiftId, f.shift_name AS shiftName,
                   s.start_time AS startTime, s.end_time AS endTime, s.work_minutes AS workMinutes,
                   s.duty_status AS dutyStatus, s.publish_status AS publishStatus,
                   IFNULL(f.is_night, 0) AS isNight
              FROM biz_schedule s
              LEFT JOIN biz_shift f ON f.id = s.shift_id
             WHERE s.del_flag = 0
            <if test="orgType != null"> AND s.org_type = #{orgType}</if>
            <if test="orgId != null"> AND s.org_id = #{orgId}</if>
            <if test="staffType != null"> AND s.staff_type = #{staffType}</if>
               AND s.schedule_date BETWEEN #{startDate} AND #{endDate}
             ORDER BY s.employee_id, s.schedule_date, s.start_time, s.id
            </script>
            """)
    List<StaffCheckRow> selectStaffCheckRows(@Param("orgType") Integer orgType,
                                             @Param("orgId") Long orgId,
                                             @Param("staffType") Integer staffType,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * 月度工时（全区岗位通用）：一人一行，含未排班补齐前的已写天数。
     * 与护理版 selectWorkload 的区别：不限护理序列，可加岗位类别过滤。
     */
    @Select("""
            <script>
            SELECT t.employee_id AS employeeId, MAX(t.emp_code) AS empCode, MAX(t.employee_name) AS employeeName,
                   MAX(t.staff_type) AS staffType,
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
                SELECT employee_id, MAX(emp_code) AS emp_code, MAX(employee_name) AS employee_name,
                       MAX(staff_type) AS staff_type, schedule_date,
                       MAX(CASE WHEN duty_status = 1 THEN 1 ELSE 0 END) AS is_work,
                       MAX(CASE WHEN duty_status = 2 THEN 1 ELSE 0 END) AS is_rest,
                       MAX(CASE WHEN duty_status = 3 THEN 1 ELSE 0 END) AS is_leave,
                       MAX(CASE WHEN duty_status = 4 THEN 1 ELSE 0 END) AS is_training,
                       MAX(CASE WHEN duty_status = 5 THEN 1 ELSE 0 END) AS is_suspended,
                       MAX(CASE WHEN duty_status = 1 AND (start_time &lt; '08:00' OR start_time &gt;= '16:00')
                                THEN 1 ELSE 0 END) AS is_night,
                       COALESCE(SUM(CASE WHEN duty_status = 1 THEN work_minutes ELSE 0 END), 0) AS minutes
                  FROM biz_schedule
                 WHERE del_flag = 0
                <if test="orgType != null"> AND org_type = #{orgType}</if>
                <if test="orgId != null"> AND org_id = #{orgId}</if>
                <if test="staffType != null"> AND staff_type = #{staffType}</if>
                   AND schedule_date BETWEEN #{startDate} AND #{endDate}
                 GROUP BY employee_id, schedule_date
              ) t
             GROUP BY t.employee_id
             ORDER BY MAX(t.emp_code), t.employee_id
            </script>
            """)
    List<StaffWorkloadVO.Workload> selectStaffWorkload(@Param("orgType") Integer orgType,
                                                       @Param("orgId") Long orgId,
                                                       @Param("staffType") Integer staffType,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    /**
     * 排班单元在册人员（用于月度工时补齐「整月未排」的人）：
     * 科室口径按 dept_id 取，病区口径按病区所属科室取；全院口径人太多不在本接口处理。
     * 岗位类别按员工岗位角色上的 staff_type 过滤。
     */
    @Select("""
            <script>
            SELECT e.id AS employeeId, e.emp_code AS empCode, e.emp_name AS employeeName,
                   MAX(r.staff_type) AS staffType
              FROM sys_employee e
              JOIN sys_employee_post p ON p.employee_id = e.id
              JOIN sys_role r ON r.id = p.role_id
             WHERE e.del_flag = 0 AND e.status = 1
               AND r.del_flag = 0 AND r.status = 1
               AND (
                 ( #{orgType} = 1 AND e.dept_id = #{orgId} )
                 OR ( #{orgType} = 2 AND e.dept_id = (SELECT dept_id FROM sys_ward WHERE ward_id = #{orgId} AND status = 1) )
               )
            <if test="staffType != null"> AND r.staff_type = #{staffType}</if>
             GROUP BY e.id, e.emp_code, e.emp_name
             ORDER BY e.emp_code, e.id
            </script>
            """)
    List<StaffWorkloadVO.Workload> selectUnitStaff(@Param("orgType") Integer orgType,
                                                   @Param("orgId") Long orgId,
                                                   @Param("staffType") Integer staffType);
}
