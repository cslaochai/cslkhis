package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.entity.BizNursingQcIndicator;
import com.his.patient.vo.NurseQcVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 护理质量指标月度台账 Mapper（sql/168）。
 */
@Mapper
public interface BizNursingQcIndicatorMapper extends BaseMapper<BizNursingQcIndicator> {

    String LEDGER_COLUMNS = """
            i.id, i.ward_id AS wardId, i.ward_name AS wardName, i.dept_id AS deptId, i.dept_name AS deptName,
            i.stat_month AS statMonth, i.indicator_code AS indicatorCode, i.indicator_name AS indicatorName,
            i.unit, i.numerator, i.denominator, i.rate_value AS rateValue, i.target_value AS targetValue,
            i.reached_flag AS reachedFlag, i.source_type AS sourceType, i.report_status AS reportStatus,
            DATE_FORMAT(i.calc_time, '%Y-%m-%d %H:%i:%s') AS calcTime, i.remark""";

    /**
     * 指标值放大倍数：{@code %} 类乘 100，例/千床日类乘 1000（跟台账 unit 列同源，不再传参）
     */
    String RATE_EXPR = """
            ROUND(SUM(numerator) * IF(MAX(unit) = '%', 100, 1000) / NULLIF(SUM(denominator), 0), 2)""";

    // 事实取数（重算的分子分母来源）

    /**
     * 某病区某月的<b>实际占用床日数</b>（口径见类注释，与 sql/168 铺底逐字一致）
     */
    @Select("""
            SELECT COALESCE(SUM(GREATEST(DATEDIFF(
                     LEAST(COALESCE(DATE(a.discharge_time), #{statEnd}), #{statEnd}),
                     GREATEST(DATE(a.admit_time), #{monthStart})), 1)), 0)
              FROM biz_admission a
             WHERE a.del_flag = 0 AND a.ward_id = #{wardId}
               AND DATE(a.admit_time) <= #{statEnd}
               AND (a.discharge_time IS NULL OR DATE(a.discharge_time) >= #{monthStart})
            """)
    int selectBedDays(@Param("wardId") Long wardId,
                      @Param("monthStart") LocalDate monthStart,
                      @Param("statEnd") LocalDate statEnd);

    /**
     * 某病区某月的不良事件例数（千床日率的分子）。
     *
     * <p>月份按 {@code DATE(occur_time)} 落月，不按 create_time —— 事件是几点发生的才是事实，
     * 什么时候补录的只是行政痕迹。
     * <br>{@code acquiredFlag} 传 null 表示不区分（跌倒），传 1 只算院内获得（压疮）。
     */
    @Select("""
            <script>
            SELECT COUNT(*)
              FROM biz_adverse_event e
             WHERE e.del_flag = 0 AND e.event_type = #{eventType} AND e.occur_ward_id = #{wardId}
               AND DATE(e.occur_time) BETWEEN #{monthStart} AND #{statEnd}
            <if test="acquiredFlag != null"> AND e.acquired_flag = #{acquiredFlag}</if>
            </script>
            """)
    int selectEventCount(@Param("wardId") Long wardId,
                         @Param("monthStart") LocalDate monthStart,
                         @Param("statEnd") LocalDate statEnd,
                         @Param("eventType") Integer eventType,
                         @Param("acquiredFlag") Integer acquiredFlag);

    /**
     * 当月有台账的病区（重算「全部病区」时的候选：只列启用了质控指标口径的病区，即病区启用行）
     */
    @Select("""
            <script>
            SELECT w.ward_id AS wardId, w.ward_name AS wardName, w.dept_id AS deptId, d.dept_name AS deptName
              FROM sys_ward w
              LEFT JOIN sys_department d ON d.id = w.dept_id
             WHERE w.status = 1
            <if test="deptIds != null">
              AND w.dept_id IN <foreach collection="deptIds" item="x" open="(" separator="," close=")">#{x}</foreach>
            </if>
             ORDER BY w.dept_id, w.ward_code, w.ward_id
            </script>
            """)
    List<NurseQcVO.Ward> selectCalcWards(@Param("deptIds") List<Long> deptIds);

    /**
     * 该月该病区<b>已上报</b>的指标编码：重算逐条跳过它们，
     * 这样「一键重算」绝不会把已经写进护理部月度通报的数字悄悄改掉（消息里也要如实报出跳过几条）。
     */
    @Select("""
            SELECT indicator_code
              FROM biz_nursing_qc_indicator
             WHERE del_flag = 0 AND report_status = 2 AND stat_month = #{statMonth}
               AND ward_id = #{wardId}
            """)
    List<String> selectReportedCodes(@Param("statMonth") String statMonth, @Param("wardId") Long wardId);

    /**
     * 台账 upsert：命中唯一键 {@code uk_indicator(ward_id, stat_month, indicator_code)} 就整行覆盖，
     * 但<b>每个可更新列都被 {@code report_status = 2} 挡住</b>。
     *
     * <p>为什么把闸门写在 SQL 而不是只写在 Java：服务层虽然已经先查了 {@code selectReportedCodes}，
     * 但两个人同时点重算时那一查一写之间有窗口；把条件压进一条语句里才是真的挡得住。
     * <br>报告状态本身不在更新列里 —— 重算无权改变上报状态，上报/退回走 {@link #updateReportStatus}。
     *
     * <p>⚠ 更新表达式里的<b>旧值列必须带上表名限定</b>：一旦写了 {@code VALUES (...) AS new}，
     * {@code new} 就是一张参与解析的表，裸列名报告状态会同时命中新旧两行 →
     * MySQL 报「Column 'report_status' in field list is ambiguous」被兜成 500，
     * 而且只在<b>命中唯一键</b>时才报错（纯新增的月份跑得好好的，一到已入账的月份就炸）。
     */
    @Insert("""
            INSERT INTO biz_nursing_qc_indicator
              (id, ward_id, ward_name, dept_id, dept_name, stat_month, indicator_code, indicator_name, unit,
               numerator, denominator, rate_value, target_value, reached_flag, source_type, report_status,
               calc_time, create_by, create_time, update_by, update_time, remark)
            VALUES (#{id}, #{wardId}, #{wardName}, #{deptId}, #{deptName}, #{statMonth}, #{indicatorCode},
                    #{indicatorName}, #{unit}, #{numerator}, #{denominator}, #{rateValue}, #{targetValue},
                    #{reachedFlag}, #{sourceType}, #{reportStatus}, #{calcTime}, #{createBy}, NOW(),
                    #{updateBy}, NOW(), #{remark}) AS new
            ON DUPLICATE KEY UPDATE
                   ward_name = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.ward_name, new.ward_name),
                   dept_id = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.dept_id, new.dept_id),
                   dept_name = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.dept_name, new.dept_name),
                   indicator_name = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.indicator_name, new.indicator_name),
                   unit = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.unit, new.unit),
                   numerator = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.numerator, new.numerator),
                   denominator = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.denominator, new.denominator),
                   rate_value = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.rate_value, new.rate_value),
                   target_value = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.target_value, new.target_value),
                   reached_flag = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.reached_flag, new.reached_flag),
                   source_type = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.source_type, new.source_type),
                   calc_time = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.calc_time, new.calc_time),
                   update_by = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.update_by, new.update_by),
                   update_time = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.update_time, new.update_time),
                   remark = IF(biz_nursing_qc_indicator.report_status = 2, biz_nursing_qc_indicator.remark, new.remark)
            """)
    int upsertIndicator(BizNursingQcIndicator row);

    /**
     * 上报 / 退回：整月（或单病区）批量翻报告状态。
     *
     * <p>只翻状态，不动任何数字 —— 已上报的行要被重算跳过，所以「退回未上报」是把某月拉回可修订状态
     * 的唯一入口（改完事实来源再重算再上报）。
     */
    @Update("""
            <script>
            UPDATE biz_nursing_qc_indicator
               SET report_status = #{reportStatus}, update_by = #{operator}, update_time = NOW()
             WHERE del_flag = 0 AND stat_month = #{statMonth} AND report_status &lt;&gt; #{reportStatus}
            <if test="wardId != null"> AND ward_id = #{wardId}</if>
            <if test="deptIds != null">
               AND dept_id IN <foreach collection="deptIds" item="x" open="(" separator="," close=")">#{x}</foreach>
            </if>
            </script>
            """)
    int updateReportStatus(@Param("statMonth") String statMonth,
                           @Param("wardId") Long wardId,
                           @Param("reportStatus") Integer reportStatus,
                           @Param("deptIds") List<Long> deptIds,
                           @Param("operator") String operator);

    // 看板取数

    /**
     * 月度 KPI：按指标聚合（{@code wardId} 传具体病区=单病区，传 null=可见范围全院合并）。
     *
     * <p>全院合格率用 SUM(分子)/SUM(分母) 而不是各病区的算术平均 ——
     * 抽查 20 例的病区和抽查 60 例的病区权重不同，平均会把小病区的问题放大。
     * {@code notReachedCount} 是未达标病区数，页面在「全院 96%」旁边还要看得见「2 个病区未达标」。
     */
    @Select("""
            <script>
            SELECT indicator_code AS indicatorCode, MAX(indicator_name) AS indicatorName, MAX(unit) AS unit,
                   SUM(numerator) AS numerator, SUM(denominator) AS denominator,
            """ + RATE_EXPR + """
                   AS rateValue,
                   MAX(target_value) AS targetValue, MAX(source_type) AS sourceType,
                   COUNT(*) AS wardCount, SUM(reached_flag = 0) AS notReachedCount,
                   SUM(report_status = 2) AS reportedCount, SUM(report_status = 1) AS unreportedCount
              FROM biz_nursing_qc_indicator
             WHERE del_flag = 0 AND stat_month = #{statMonth}
            <if test="wardId != null"> AND ward_id = #{wardId}</if>
            <if test="deptIds != null">
              AND dept_id IN <foreach collection="deptIds" item="x" open="(" separator="," close=")">#{x}</foreach>
            </if>
             GROUP BY indicator_code
             ORDER BY indicator_code
            </script>
            """)
    List<NurseQcVO.Kpi> selectMonthKpi(@Param("statMonth") String statMonth,
                                       @Param("wardId") Long wardId,
                                       @Param("deptIds") List<Long> deptIds);

    /**
     * 趋势：一条指标按月一行（月份区间空=全部有台账的月份，前端折线直接用）
     */
    @Select("""
            <script>
            SELECT stat_month AS statMonth, indicator_code AS indicatorCode, MAX(indicator_name) AS indicatorName,
                   MAX(unit) AS unit, SUM(numerator) AS numerator, SUM(denominator) AS denominator,
            """ + RATE_EXPR + """
                   AS rateValue,
                   MAX(target_value) AS targetValue, COUNT(*) AS wardCount,
                   SUM(reached_flag = 0) AS notReachedCount
              FROM biz_nursing_qc_indicator
             WHERE del_flag = 0 AND indicator_code = #{indicatorCode}
            <if test="wardId != null"> AND ward_id = #{wardId}</if>
            <if test="startMonth != null and startMonth != ''"> AND stat_month &gt;= #{startMonth}</if>
            <if test="endMonth != null and endMonth != ''"> AND stat_month &lt;= #{endMonth}</if>
            <if test="deptIds != null">
              AND dept_id IN <foreach collection="deptIds" item="x" open="(" separator="," close=")">#{x}</foreach>
            </if>
             GROUP BY stat_month, indicator_code
             ORDER BY stat_month
            </script>
            """)
    List<NurseQcVO.Kpi> selectTrend(@Param("indicatorCode") String indicatorCode,
                                    @Param("wardId") Long wardId,
                                    @Param("startMonth") String startMonth,
                                    @Param("endMonth") String endMonth,
                                    @Param("deptIds") List<Long> deptIds);

    /**
     * 病区对比：一条指标在当月各病区的落点（按指标值倒序，页面上「最差的那个病区」永远在最上面）
     */
    @Select("""
            <script>
            SELECT i.ward_id AS wardId, i.ward_name AS wardName, i.dept_id AS deptId, i.dept_name AS deptName,
                   i.stat_month AS statMonth, i.indicator_code AS indicatorCode, i.indicator_name AS indicatorName,
                   i.unit, i.numerator, i.denominator, i.rate_value AS rateValue, i.target_value AS targetValue,
                   i.reached_flag AS reachedFlag, i.source_type AS sourceType, i.report_status AS reportStatus
              FROM biz_nursing_qc_indicator i
             WHERE i.del_flag = 0 AND i.stat_month = #{statMonth} AND i.indicator_code = #{indicatorCode}
            <if test="deptIds != null">
              AND i.dept_id IN <foreach collection="deptIds" item="x" open="(" separator="," close=")">#{x}</foreach>
            </if>
             ORDER BY i.rate_value DESC, i.ward_id
            </script>
            """)
    List<NurseQcVO.LedgerRow> selectWardCompare(@Param("statMonth") String statMonth,
                                                @Param("indicatorCode") String indicatorCode,
                                                @Param("deptIds") List<Long> deptIds);

    /**
     * 台账分页（护理部回看某月/跨月的分子分母明细，每一行都要能回答「这个数从哪来」）
     */
    @Select("""
            <script>
            """ + "SELECT " + LEDGER_COLUMNS + """
              FROM biz_nursing_qc_indicator i
             WHERE i.del_flag = 0
            <if test="keyword != null and keyword != ''">
              AND (i.ward_name LIKE CONCAT('%', #{keyword}, '%')
                OR i.indicator_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            <if test="wardId != null"> AND i.ward_id = #{wardId}</if>
            <if test="deptId != null"> AND i.dept_id = #{deptId}</if>
            <if test="indicatorCode != null and indicatorCode != ''"> AND i.indicator_code = #{indicatorCode}</if>
            <if test="reportStatus != null"> AND i.report_status = #{reportStatus}</if>
            <if test="statMonth != null and statMonth != ''"> AND i.stat_month = #{statMonth}</if>
            <if test="startMonth != null and startMonth != ''"> AND i.stat_month &gt;= #{startMonth}</if>
            <if test="endMonth != null and endMonth != ''"> AND i.stat_month &lt;= #{endMonth}</if>
            <if test="deptIds != null">
              AND i.dept_id IN <foreach collection="deptIds" item="x" open="(" separator="," close=")">#{x}</foreach>
            </if>
             ORDER BY i.stat_month DESC, i.ward_id, i.indicator_code
            </script>
            """)
    List<NurseQcVO.LedgerRow> selectLedgerPage(IPage<NurseQcVO.LedgerRow> page,
                                               @Param("keyword") String keyword,
                                               @Param("wardId") Long wardId,
                                               @Param("deptId") Long deptId,
                                               @Param("indicatorCode") String indicatorCode,
                                               @Param("reportStatus") Integer reportStatus,
                                               @Param("statMonth") String statMonth,
                                               @Param("startMonth") String startMonth,
                                               @Param("endMonth") String endMonth,
                                               @Param("deptIds") List<Long> deptIds);

    @Select("SELECT " + LEDGER_COLUMNS + """
              FROM biz_nursing_qc_indicator i
             WHERE i.del_flag = 0 AND i.id = #{id}
            """)
    NurseQcVO.LedgerRow selectLedgerById(@Param("id") Long id);

    /**
     * 物理删台账行（唯一键不含 del_flag ⇒ 软删会让「删掉这行再重算同一月同一指标」撞键）。
     * 台账本来就是重算出来的结果账，没有留档价值，删了再算即可。
     */
    @Delete("DELETE FROM biz_nursing_qc_indicator WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
