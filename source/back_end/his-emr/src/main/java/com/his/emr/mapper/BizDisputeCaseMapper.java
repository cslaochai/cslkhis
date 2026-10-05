package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.emr.entity.BizDisputeCase;
import com.his.emr.vo.DisputeCaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 纠纷/投诉主单 Mapper。
 *
 * <p>跨模块读（科室 / 患者基本信息 / 入院记录 / 病历归档）
 * 按仓库约定走裸 SQL，不引入模块依赖；列名以 information_schema 实查为准。
 */
@Mapper
public interface BizDisputeCaseMapper extends BaseMapper<BizDisputeCase> {

    /**
     * 分页（关键字模糊单号/患者/投诉人；类型/状态/等级/科室/未结案/登记日期区间）
     *
     * <p>日期上界必须补 23:59:59：create_time 是 DATETIME，直接用 'yyyy-MM-dd' 比较会把当天全部时点滤掉。
     */
    @Select("""
            <script>
            SELECT c.*
              FROM biz_dispute_case c
             WHERE c.del_flag = 0
               <if test="keyword != null and keyword != ''">
                 AND (c.case_no LIKE CONCAT('%', #{keyword}, '%')
                   OR c.patient_name LIKE CONCAT('%', #{keyword}, '%')
                   OR c.complainant LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="caseType != null"> AND c.case_type = #{caseType}</if>
               <if test="status != null"> AND c.status = #{status}</if>
               <if test="level != null"> AND c.level = #{level}</if>
               <if test="deptId != null"> AND c.dept_id = #{deptId}</if>
               <if test="openOnly != null and openOnly == true"> AND c.status IN (1, 2, 3)</if>
               <if test="dateFrom != null and dateFrom != ''"> AND c.create_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND c.create_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
             ORDER BY c.status ASC, c.id DESC
            </script>
            """)
    List<DisputeCaseVO> selectCasePage(IPage<DisputeCaseVO> page,
                                       @Param("keyword") String keyword,
                                       @Param("caseType") Integer caseType,
                                       @Param("status") Integer status,
                                       @Param("level") Integer level,
                                       @Param("deptId") Long deptId,
                                       @Param("openOnly") Boolean openOnly,
                                       @Param("dateFrom") String dateFrom,
                                       @Param("dateTo") String dateTo);

    @Select("SELECT c.* FROM biz_dispute_case c WHERE c.id = #{id} AND c.del_flag = 0")
    DisputeCaseVO selectCaseById(@Param("id") Long id);

    /**
     * 患者快照（患者基本信息跨模块裸 SQL；取不到返回 NULL，由上层抛业务异常，不编造）
     */
    @Select("SELECT p.patient_no FROM biz_patient p WHERE p.id = #{patientId} AND p.del_flag = 0")
    String selectPatientNo(@Param("patientId") Long patientId);

    @Select("SELECT p.patient_name FROM biz_patient p WHERE p.id = #{patientId} AND p.del_flag = 0")
    String selectPatientName(@Param("patientId") Long patientId);

    /**
     * 科室名快照（科室跨模块裸 SQL）
     */
    @Select("SELECT d.dept_name FROM sys_department d WHERE d.id = #{deptId} AND d.del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    /**
     * 该患者当前可封存的病案（仅「已归档」archive_status=2；已封存3 不重复封、待归档1 封不了）。
     *
     * <p>必须用 orderBy + LIMIT 1 而不是 MP getOne —— getOne 命中多行直接抛异常。
     */
    @Select("SELECT a.id FROM biz_medical_record_archive a " +
            " WHERE a.del_flag = 0 AND a.archive_status = 2 AND a.patient_id = #{patientId} " +
            " ORDER BY a.id DESC LIMIT 1")
    Long selectSealableArchiveId(@Param("patientId") Long patientId);

    // 统计（服务端 group by，不让前端数当前页）

    @Select("""
            <script>
            SELECT c.status AS k, COUNT(*) AS c
              FROM biz_dispute_case c
             WHERE c.del_flag = 0
               <if test="dateFrom != null and dateFrom != ''"> AND c.create_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND c.create_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
             GROUP BY c.status
            </script>
            """)
    List<java.util.Map<String, Object>> countByStatus(@Param("dateFrom") String dateFrom,
                                                      @Param("dateTo") String dateTo);

    @Select("""
            <script>
            SELECT c.case_type AS k, COUNT(*) AS c
              FROM biz_dispute_case c
             WHERE c.del_flag = 0
               <if test="dateFrom != null and dateFrom != ''"> AND c.create_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND c.create_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
             GROUP BY c.case_type ORDER BY c DESC
            </script>
            """)
    List<java.util.Map<String, Object>> countByCaseType(@Param("dateFrom") String dateFrom,
                                                        @Param("dateTo") String dateTo);

    @Select("""
            <script>
            SELECT COALESCE(c.dept_id, 0) AS d, COALESCE(c.dept_name, '未指定科室') AS n, COUNT(*) AS c
              FROM biz_dispute_case c
             WHERE c.del_flag = 0
               <if test="dateFrom != null and dateFrom != ''"> AND c.create_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND c.create_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
             GROUP BY COALESCE(c.dept_id, 0), COALESCE(c.dept_name, '未指定科室')
             ORDER BY c DESC, d ASC LIMIT 10
            </script>
            """)
    List<java.util.Map<String, Object>> countByDeptTop(@Param("dateFrom") String dateFrom,
                                                       @Param("dateTo") String dateTo);

    /**
     * 已结案单据的赔偿合计与平均结案天数（未结案不参与平均）
     */
    @Select("""
            <script>
            SELECT COALESCE(SUM(c.compensation), 0) AS total,
                   COALESCE(ROUND(AVG(TIMESTAMPDIFF(DAY, c.accept_time, c.close_time)), 1), 0) AS avg_days
              FROM biz_dispute_case c
             WHERE c.del_flag = 0 AND c.status = 4
               <if test="dateFrom != null and dateFrom != ''"> AND c.create_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND c.create_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
            </script>
            """)
    java.util.Map<String, Object> sumClosed(@Param("dateFrom") String dateFrom,
                                            @Param("dateTo") String dateTo);
}
