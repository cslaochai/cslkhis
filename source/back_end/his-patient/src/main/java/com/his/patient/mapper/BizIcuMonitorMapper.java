package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizIcuMonitor;
import com.his.patient.vo.IcuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ICU 监护记录 Mapper。
 */
@Mapper
public interface BizIcuMonitorMapper extends BaseMapper<BizIcuMonitor> {

    String MONITOR_COLUMNS = """
             m.*, s.stay_no, s.patient_name, s.bed_no, s.status AS stayStatus
            """;

    @Select("""
            <script>
            SELECT """ + MONITOR_COLUMNS + """
              FROM biz_icu_monitor m
              JOIN biz_icu_stay s ON s.id = m.stay_id
             WHERE m.del_flag = 0
               <if test="stayId != null"> AND m.stay_id = #{stayId}</if>
               <if test="startDate != null"> AND m.record_date &gt;= #{startDate}</if>
               <if test="endDate != null"> AND m.record_date &lt;= #{endDate}</if>
             ORDER BY m.record_time DESC, m.id DESC
            </script>
            """)
    List<IcuVO.MonitorVO> selectMonitorPage(IPage<IcuVO.MonitorVO> page,
                                            @Param("stayId") Long stayId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * 单条入科记录的监护趋势（按时间正序，曲线与表格共用）
     */
    @Select("""
            <script>
            SELECT """ + MONITOR_COLUMNS + """
              FROM biz_icu_monitor m
              JOIN biz_icu_stay s ON s.id = m.stay_id
             WHERE m.del_flag = 0 AND m.stay_id = #{stayId}
               <if test="since != null"> AND m.record_time &gt;= #{since}</if>
             ORDER BY m.record_time ASC, m.id ASC
             <if test="limit != null"> LIMIT #{limit}</if>
            </script>
            """)
    List<IcuVO.MonitorVO> selectMonitorTrend(@Param("stayId") Long stayId,
                                            @Param("since") LocalDateTime since,
                                            @Param("limit") Integer limit);

    @Select("""
            SELECT """ + MONITOR_COLUMNS + """
              FROM biz_icu_monitor m
              JOIN biz_icu_stay s ON s.id = m.stay_id
             WHERE m.del_flag = 0 AND m.id = #{id}
            """)
    IcuVO.MonitorVO selectMonitorById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM biz_icu_monitor WHERE del_flag = 0 AND stay_id = #{stayId}")
    int countByStay(@Param("stayId") Long stayId);

    /**
     * 同一入科记录下该时刻是否已有记录（撞 uk_icu_monitor_time 前的业务提示）
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_icu_monitor
             WHERE del_flag = 0 AND stay_id = #{stayId} AND record_time = #{recordTime}
               <if test="excludeId != null"> AND id &lt;&gt; #{excludeId}</if>
            </script>
            """)
    int countAtTime(@Param("stayId") Long stayId,
                    @Param("recordTime") LocalDateTime recordTime,
                    @Param("excludeId") Long excludeId);

    @Select("""
            SELECT COUNT(*) FROM biz_icu_monitor
             WHERE del_flag = 0 AND record_time BETWEEN #{startDateTime} AND #{endDateTime}
            """)
    int countRange(@Param("startDateTime") LocalDateTime startDateTime,
                   @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 在科患者「最近一条记录」的呼吸支持方式分布
     */
    @Select("""
            SELECT m.vent_mode AS type, COUNT(*) AS count
              FROM biz_icu_monitor m
              JOIN biz_icu_stay s ON s.id = m.stay_id AND s.del_flag = 0 AND s.status = 1
             WHERE m.del_flag = 0 AND m.vent_mode IS NOT NULL
               AND m.id = (SELECT m2.id FROM biz_icu_monitor m2
                            WHERE m2.del_flag = 0 AND m2.stay_id = s.id
                            ORDER BY m2.record_time DESC, m2.id DESC LIMIT 1)
             GROUP BY m.vent_mode
             ORDER BY m.vent_mode ASC
            """)
    List<IcuVO.TypeCount> selectLatestVentModes();

    /**
     * 现带管人数（按在科患者最近一条记录判定）
     */
    @Select("""
            SELECT SUM(CASE WHEN m.has_airway = 1 THEN 1 ELSE 0 END)   AS airway_count,
                   SUM(CASE WHEN m.has_cvc = 1 THEN 1 ELSE 0 END)      AS cvc_count,
                   SUM(CASE WHEN m.has_arterial = 1 THEN 1 ELSE 0 END) AS arterial_count,
                   SUM(CASE WHEN m.has_catheter = 1 THEN 1 ELSE 0 END) AS catheter_count,
                   SUM(CASE WHEN m.has_drain = 1 THEN 1 ELSE 0 END)    AS drain_count
              FROM biz_icu_monitor m
              JOIN biz_icu_stay s ON s.id = m.stay_id AND s.del_flag = 0 AND s.status = 1
             WHERE m.del_flag = 0
               AND m.id = (SELECT m2.id FROM biz_icu_monitor m2
                            WHERE m2.del_flag = 0 AND m2.stay_id = s.id
                            ORDER BY m2.record_time DESC, m2.id DESC LIMIT 1)
            """)
    IcuVO.StatsVO selectTubeSummary();

    /**
     * ICU 开放床位数（bed_type='ICU' 且未停用）
     */
    @Select("SELECT COUNT(*) FROM sys_bed WHERE del_flag = 0 AND bed_type = 'ICU' AND bed_status <> 0")
    int countIcuBeds();
}
