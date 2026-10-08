package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.entity.BizDialysisSession;
import com.his.medicaltech.vo.DialysisVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 透析单 Mapper。
 */
@Mapper
public interface BizDialysisSessionMapper extends BaseMapper<BizDialysisSession> {

    String SESSION_COLUMNS = """
             s.*, m.room_name
            """;

    @Select("""
            <script>
            SELECT """ + SESSION_COLUMNS + """
              FROM biz_dialysis_session s
              LEFT JOIN biz_dialysis_machine m ON m.id = s.machine_id
             WHERE s.del_flag = 0
               <if test="sessionNo != null and sessionNo != ''">
                 AND s.session_no LIKE CONCAT('%', #{sessionNo}, '%')
               </if>
               <if test="patientName != null and patientName != ''">
                 AND s.patient_name LIKE CONCAT('%', #{patientName}, '%')
               </if>
               <if test="startDate != null"> AND s.dialysis_date &gt;= #{startDate}</if>
               <if test="endDate != null"> AND s.dialysis_date &lt;= #{endDate}</if>
               <if test="timeSlot != null"> AND s.time_slot = #{timeSlot}</if>
               <if test="machineId != null"> AND s.machine_id = #{machineId}</if>
               <if test="archiveId != null"> AND s.archive_id = #{archiveId}</if>
               <if test="status != null"> AND s.status = #{status}</if>
             ORDER BY s.dialysis_date DESC, s.time_slot ASC, s.id DESC
            </script>
            """)
    List<DialysisVO.SessionVO> selectSessionPage(IPage<DialysisVO.SessionVO> page,
                                                 @Param("sessionNo") String sessionNo,
                                                 @Param("patientName") String patientName,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("timeSlot") Integer timeSlot,
                                                 @Param("machineId") Long machineId,
                                                 @Param("archiveId") Long archiveId,
                                                 @Param("status") Integer status);

    @Select("""
            SELECT """ + SESSION_COLUMNS + """
              FROM biz_dialysis_session s
              LEFT JOIN biz_dialysis_machine m ON m.id = s.machine_id
             WHERE s.del_flag = 0 AND s.id = #{id}
            """)
    DialysisVO.SessionVO selectSessionById(@Param("id") Long id);

    /**
     * 机位时段看板：全部机位（含维修/停用）左连当日该时段的透析单，空格也占一格。
     */
    @Select("""
            SELECT m.id            AS machine_id,
                   m.machine_no    AS machine_no,
                   m.room_name     AS room_name,
                   m.status        AS machine_status,
                   s.id            AS session_id,
                   s.session_no    AS session_no,
                   s.status        AS session_status,
                   s.patient_id    AS patient_id,
                   s.patient_no    AS patient_no,
                   s.patient_name  AS patient_name,
                   s.before_weight AS before_weight,
                   s.after_weight  AS after_weight,
                   s.ultra_ml      AS ultra_ml,
                   s.adverse_type  AS adverse_type
              FROM biz_dialysis_machine m
              LEFT JOIN biz_dialysis_session s
                     ON s.machine_id = m.id AND s.del_flag = 0 AND s.status <> 4
                    AND s.dialysis_date = #{date} AND s.time_slot = #{timeSlot}
             WHERE m.del_flag = 0
             ORDER BY m.machine_no ASC
            """)
    List<DialysisVO.BoardCellVO> selectBoardSlot(@Param("date") LocalDate date,
                                                 @Param("timeSlot") Integer timeSlot);

    /**
     * 该时段的机位占用者（排班撞车时给出人话提示）
     */
    @Select("""
            SELECT """ + SESSION_COLUMNS + """
              FROM biz_dialysis_session s
              LEFT JOIN biz_dialysis_machine m ON m.id = s.machine_id
             WHERE s.del_flag = 0 AND s.status <> 4
               AND s.dialysis_date = #{date}
               AND s.time_slot = #{timeSlot} AND s.machine_id = #{machineId}
             LIMIT 1
            """)
    DialysisVO.SessionVO selectSlotOccupant(@Param("date") LocalDate date,
                                            @Param("timeSlot") Integer timeSlot,
                                            @Param("machineId") Long machineId);

    /**
     * 该档案未结束的透析单数（在透状态变更/退档前的拦截依据）
     */
    @Select("SELECT COUNT(*) FROM biz_dialysis_session WHERE del_flag = 0 AND archive_id = #{archiveId} AND status IN (1, 2)")
    int countUnfinished(@Param("archiveId") Long archiveId);

    /**
     * 工作量汇总（一次条件聚合出全部计数）
     */
    @Select("""
            SELECT COUNT(*)                                                        AS session_total,
                   SUM(CASE WHEN s.status = 3 THEN 1 ELSE 0 END)                   AS done_count,
                   SUM(CASE WHEN s.status = 4 THEN 1 ELSE 0 END)                   AS cancelled_count,
                   SUM(CASE WHEN s.status = 2 THEN 1 ELSE 0 END)                   AS on_machine_count,
                   SUM(CASE WHEN s.status = 1 THEN 1 ELSE 0 END)                   AS scheduled_count,
                   SUM(CASE WHEN s.adverse_type IS NOT NULL THEN 1 ELSE 0 END)     AS adverse_count,
                   ROUND(AVG(CASE WHEN s.status = 3 THEN s.ultra_ml END), 1)       AS avg_ultra_ml,
                   ROUND(AVG(CASE WHEN s.status = 3 THEN s.actual_duration_min END), 0) AS avg_actual_duration_min
              FROM biz_dialysis_session s
             WHERE s.del_flag = 0 AND s.dialysis_date BETWEEN #{startDate} AND #{endDate}
            """)
    DialysisVO.StatsVO selectStatsSummary(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT s.adverse_type AS type, COUNT(*) AS count
              FROM biz_dialysis_session s
             WHERE s.del_flag = 0 AND s.adverse_type IS NOT NULL
               AND s.dialysis_date BETWEEN #{startDate} AND #{endDate}
             GROUP BY s.adverse_type
             ORDER BY COUNT(*) DESC, s.adverse_type ASC
            """)
    List<DialysisVO.TypeCount> selectAdverseTypes(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT m.machine_no AS machine_no, COUNT(*) AS count
              FROM biz_dialysis_session s
              JOIN biz_dialysis_machine m ON m.id = s.machine_id
             WHERE s.del_flag = 0 AND s.status <> 4
               AND s.dialysis_date BETWEEN #{startDate} AND #{endDate}
             GROUP BY m.machine_no
             ORDER BY count DESC, m.machine_no ASC
            """)
    List<DialysisVO.MachineLoad> selectMachineLoads(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}
