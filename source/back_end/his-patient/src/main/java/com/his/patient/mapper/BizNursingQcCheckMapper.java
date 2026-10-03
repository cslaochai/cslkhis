package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizNursingQcCheck;
import com.his.patient.vo.NurseQcVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 护理质量检查单 Mapper（sql/168）。
 *
 * <p>病区与检查人取自病区/员工，它们归系统域，按项目规范不跨域调
 * 它们的 Mapper，这里走裸 SQL 只读。<b>病区没有 del_flag 列</b>，别给它加条件
 * （加了直接 Unknown column 报 500）。
 *
 * <p>自定义 SQL 不受 {@code @TableLogic} 覆盖，凡读护理质量检查单都要手写
 * {@code del_flag = 0}。
 */
@Mapper
public interface BizNursingQcCheckMapper extends BaseMapper<BizNursingQcCheck> {

    String CHECK_COLUMNS = """
            c.id, c.check_no AS checkNo, c.ward_id AS wardId, c.ward_name AS wardName,
            c.dept_id AS deptId, c.dept_name AS deptName, c.check_month AS checkMonth,
            DATE_FORMAT(c.check_date, '%Y-%m-%d') AS checkDate, c.category,
            c.inspector_id AS inspectorId, c.inspector_name AS inspectorName,
            c.sample_count AS sampleCount, c.qualified_count AS qualifiedCount,
            c.qualified_rate AS qualifiedRate, c.full_score AS fullScore,
            c.total_score AS totalScore, c.score_rate AS scoreRate, c.status, c.summary,
            DATE_FORMAT(c.create_time, '%Y-%m-%d %H:%i:%s') AS createTime""";

    /** 病区下拉（护理质控页的病区筛选：只列当前岗位可见科室下的启用病区，附床位看得到规模） */
    @Select("""
            <script>
            SELECT w.ward_id AS wardId, w.ward_code AS wardCode, w.ward_name AS wardName,
                   w.dept_id AS deptId, d.dept_name AS deptName,
                   w.total_beds AS totalBeds, w.occupied_beds AS occupiedBeds
              FROM sys_ward w
              LEFT JOIN sys_department d ON d.id = w.dept_id
             WHERE w.status = 1
            <if test="deptIds != null">
              AND w.dept_id IN <foreach collection="deptIds" item="x" open="(" separator="," close=")">#{x}</foreach>
            </if>
            <if test="keyword != null and keyword != ''">
              AND (w.ward_name LIKE CONCAT('%', #{keyword}, '%') OR w.ward_code LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY w.dept_id, w.ward_code, w.ward_id
            </script>
            """)
    List<NurseQcVO.Ward> selectWardOptions(@Param("deptIds") List<Long> deptIds,
                                           @Param("keyword") String keyword);

    /** 病区快照（写检查单前必查：存在且启用，并用它的 dept 做数据范围收口） */
    @Select("""
            SELECT w.ward_id AS wardId, w.ward_code AS wardCode, w.ward_name AS wardName,
                   w.dept_id AS deptId, d.dept_name AS deptName,
                   w.total_beds AS totalBeds, w.occupied_beds AS occupiedBeds
              FROM sys_ward w
              LEFT JOIN sys_department d ON d.id = w.dept_id
             WHERE w.ward_id = #{wardId} AND w.status = 1
            """)
    NurseQcVO.Ward selectWard(@Param("wardId") Long wardId);

    /**
     * 检查人候选（病区所属科室的在职人员）：质控单由护士长或护理部质控组签，
     * 不限 {@code emp_type} —— 护理部下来抽查的可能是纯管理岗，卡护理岗会选不出人。
     */
    @Select("""
            <script>
            SELECT e.id AS employeeId, e.emp_code AS empCode, e.emp_name AS empName,
                   e.title AS title, e.dept_id AS deptId, e.dept_name AS deptName
              FROM sys_employee e
             WHERE e.del_flag = 0 AND e.status = 1
            <if test="deptId != null"> AND e.dept_id = #{deptId}</if>
            <if test="keyword != null and keyword != ''">
              AND (e.emp_name LIKE CONCAT('%', #{keyword}, '%') OR e.emp_code LIKE CONCAT('%', #{keyword}, '%'))
            </if>
             ORDER BY e.emp_code, e.id
             LIMIT #{limit}
            </script>
            """)
    List<NurseQcVO.Inspector> selectInspectors(@Param("deptId") Long deptId,
                                               @Param("keyword") String keyword,
                                               @Param("limit") int limit);

    /** 单个在职检查人快照（保存时重查，前端传的 id 只当定位用，姓名一律取库里的） */
    @Select("""
            SELECT e.id AS employeeId, e.emp_code AS empCode, e.emp_name AS empName,
                   e.title AS title, e.dept_id AS deptId, e.dept_name AS deptName
              FROM sys_employee e
             WHERE e.del_flag = 0 AND e.status = 1 AND e.id = #{employeeId}
            """)
    NurseQcVO.Inspector selectInspector(@Param("employeeId") Long employeeId);

    /** 检查单分页（keyword 命中单号/病区/检查人；月份按 yyyy-MM 字符串比较，字典序即时间序） */
    @Select("""
            <script>
            """ + "SELECT " + CHECK_COLUMNS + """
              FROM biz_nursing_qc_check c
             WHERE c.del_flag = 0
            <if test="keyword != null and keyword != ''">
              AND (c.check_no LIKE CONCAT('%', #{keyword}, '%')
                OR c.ward_name LIKE CONCAT('%', #{keyword}, '%')
                OR c.inspector_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="wardId != null"> AND c.ward_id = #{wardId}</if>
            <if test="deptId != null"> AND c.dept_id = #{deptId}</if>
            <if test="category != null"> AND c.category = #{category}</if>
            <if test="status != null"> AND c.status = #{status}</if>
            <if test="startMonth != null and startMonth != ''"> AND c.check_month &gt;= #{startMonth}</if>
            <if test="endMonth != null and endMonth != ''"> AND c.check_month &lt;= #{endMonth}</if>
            <if test="deptIds != null">
              AND c.dept_id IN <foreach collection="deptIds" item="x" open="(" separator="," close=")">#{x}</foreach>
            </if>
             ORDER BY c.check_month DESC, c.category, c.ward_id
            </script>
            """)
    List<NurseQcVO.CheckRow> selectCheckPage(IPage<NurseQcVO.CheckRow> page,
                                             @Param("keyword") String keyword,
                                             @Param("wardId") Long wardId,
                                             @Param("deptId") Long deptId,
                                             @Param("category") Integer category,
                                             @Param("status") Integer status,
                                             @Param("startMonth") String startMonth,
                                             @Param("endMonth") String endMonth,
                                             @Param("deptIds") List<Long> deptIds);

    @Select("SELECT " + CHECK_COLUMNS + """
              FROM biz_nursing_qc_check c
             WHERE c.del_flag = 0 AND c.id = #{id}
            """)
    NurseQcVO.CheckRow selectCheckById(@Param("id") Long id);

    /** 唯一键 {@code uk_check_ward_month_cat} 定位：同病区同月同类只能有一张单 */
    @Select("SELECT " + CHECK_COLUMNS + """
              FROM biz_nursing_qc_check c
             WHERE c.del_flag = 0 AND c.ward_id = #{wardId}
               AND c.check_month = #{checkMonth} AND c.category = #{category}
             LIMIT 1
            """)
    NurseQcVO.CheckRow selectCheckByUk(@Param("wardId") Long wardId,
                                       @Param("checkMonth") String checkMonth,
                                       @Param("category") Integer category);

    /**
     * 该病区该月有没有检查单。
     *
     * <p>只服务一件事：「全部病区」重算扫描时的入账收口 —— 全院 49 个启用病区里通常只有一小部分
     * 当月有住院事实或有检查记录，空扫一遍会给每个病区都写两行「0 床日、发生率 NULL」的台账，
     * 对比图会被四十多个无意义落点挤满。判据见 {@code NursingQcServiceImpl#recalc}。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_nursing_qc_check
             WHERE del_flag = 0 AND ward_id = #{wardId} AND check_month = #{checkMonth}
            """)
    int countByWardMonth(@Param("wardId") Long wardId, @Param("checkMonth") String checkMonth);

    /**
     * 物理删检查单。
     *
     * <p>唯一键 {@code uk_check_ward_month_cat} <b>不含 del_flag</b>：软删的行照样占键，
     * 「删掉这张单再重录同病区同月同类」会直接 Duplicate entry（AGENTS §3）。
     * 明细由服务层一并物理删（{@code uk_check_item} 同理）。
     */
    @Delete("DELETE FROM biz_nursing_qc_check WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
