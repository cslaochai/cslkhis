package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.emr.entity.BizAdverseEvent;
import com.his.emr.vo.AdverseEventStatsVO;
import com.his.emr.vo.AdverseEventVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 不良事件 Mapper
 */
@Mapper
public interface BizAdverseEventMapper extends BaseMapper<BizAdverseEvent> {

    /**
     * 不良事件分页
     * <p>
     * ⚠ ORDER BY 必须补唯一二级键 id（同秒上报顺序不稳定 → 翻页重复+丢行，且不报错）
     * ⚠ 日期过滤用 DATE(report_time)：`report_time <= '2026-09-23'` 会漏掉当天全部事件
     * ⚠ 普通 @Select（非 script）里比较符写真字符 `<>`，写 `&lt;&gt;` 会被原样发给 MySQL（G9 踩过）
     */
    @Select("<script>" +
            "SELECT e.* FROM biz_adverse_event e " +
            "WHERE e.del_flag = 0 " +
            "<if test='eventNo != null and eventNo != \"\"'> AND e.event_no LIKE CONCAT('%', #{eventNo}, '%') </if> " +
            "<if test='eventType != null'> AND e.event_type = #{eventType} </if> " +
            "<if test='eventLevel != null'> AND e.event_level = #{eventLevel} </if> " +
            "<if test='status != null'> AND e.status = #{status} </if> " +
            "<if test='occurDeptId != null'> AND e.occur_dept_id = #{occurDeptId} </if> " +
            "<if test='keyword != null and keyword != \"\"'> AND (e.title LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR e.description LIKE CONCAT('%', #{keyword}, '%') OR e.patient_name LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "<if test='dateStart != null and dateStart != \"\"'> AND DATE(e.report_time) &gt;= #{dateStart} </if> " +
            "<if test='dateEnd != null and dateEnd != \"\"'> AND DATE(e.report_time) &lt;= #{dateEnd} </if> " +
            "ORDER BY e.report_time DESC, e.id DESC" +
            "</script>")
    Page<AdverseEventVO> selectEventPage(Page<AdverseEventVO> page,
                                         @Param("eventNo") String eventNo,
                                         @Param("eventType") Integer eventType,
                                         @Param("eventLevel") Integer eventLevel,
                                         @Param("status") Integer status,
                                         @Param("occurDeptId") Long occurDeptId,
                                         @Param("keyword") String keyword,
                                         @Param("dateStart") String dateStart,
                                         @Param("dateEnd") String dateEnd);

    /**
     * 详情
     */
    @Select("SELECT e.* FROM biz_adverse_event e WHERE e.del_flag = 0 AND e.id = #{id}")
    AdverseEventVO selectEventById(@Param("id") Long id);

    /**
     * 按主键取事件并加行锁（处理/整改/结案的并发闸门）
     */
    @Select("SELECT * FROM biz_adverse_event WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    BizAdverseEvent selectByIdForUpdate(@Param("id") Long id);

    /**
     * 科室名称（跨模块读 his-system 表，铁律用裸 SQL）
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    /**
     * 患者姓名（跨模块读 his-patient 表）
     */
    @Select("SELECT patient_name FROM biz_patient WHERE id = #{patientId} AND del_flag = 0")
    String selectPatientName(@Param("patientId") Long patientId);

    /**
     * 事件发生时患者所在的<b>住院病区</b>（sql/168 补的 occur_ward_id 唯一来源）。
     *
     * <p>按「发生时刻正住在院里」筛住院记录，而不是取最近一条：压疮/跌倒要归到事发当时的那个病区，
     * 患者中途转科转病区后，拿现在的病区统计过去的事件就是错的。
     * <br>同日多段在院（转科留下多条）时优先仍在院的那段，再按入院时间倒序取一条。
     * <br>门诊患者/非患者事件查不出来返回 null —— 这些事件本来就不该进病区千床日率的分子。
     */
    @Select("SELECT a.ward_id FROM biz_admission a " +
            "WHERE a.del_flag = 0 AND a.patient_id = #{patientId} AND a.ward_id IS NOT NULL " +
            "  AND a.admit_time <= #{occurTime} " +
            "  AND (a.discharge_time IS NULL OR a.discharge_time >= #{occurTime}) " +
            "ORDER BY a.admit_status = 1 DESC, a.admit_time DESC, a.admission_id DESC LIMIT 1")
    Long selectWardIdAtTime(@Param("patientId") Long patientId,
                            @Param("occurTime") java.time.LocalDateTime occurTime);

    /**
     * 病区名称（跨模块读病区）。
     * <p>
     * ⚠ 病区没有 del_flag 列，别给它加条件（裸 SQL 不加照样查得到，加了直接 Unknown column）。
     */
    @Select("SELECT ward_name FROM sys_ward WHERE ward_id = #{wardId}")
    String selectWardName(@Param("wardId") Long wardId);

    /**
     * 工作台统计（本月口径）：
     * monthReported = 本自然月上报数；pending = 状态 1；sentinel = 本月 I 级警讯；closed = 状态 4（本月上报口径）
     */
    @Select("SELECT " +
            "  (SELECT COUNT(*) FROM biz_adverse_event WHERE del_flag = 0 " +
            "     AND report_time >= DATE_FORMAT(NOW(), '%Y-%m-01')) AS monthReported, " +
            "  (SELECT COUNT(*) FROM biz_adverse_event WHERE del_flag = 0 AND status = 1) AS pending, " +
            "  (SELECT COUNT(*) FROM biz_adverse_event WHERE del_flag = 0 AND event_level = 1 " +
            "     AND report_time >= DATE_FORMAT(NOW(), '%Y-%m-01')) AS sentinel, " +
            "  (SELECT COUNT(*) FROM biz_adverse_event WHERE del_flag = 0 AND status = 4 " +
            "     AND report_time >= DATE_FORMAT(NOW(), '%Y-%m-01')) AS closed")
    AdverseEventStatsVO selectMonthStats();
}
