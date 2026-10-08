package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.patient.dto.NutritionScreenQueryPageDTO;
import com.his.patient.entity.BizNutritionScreen;
import com.his.patient.vo.NutritionScreenVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 营养风险筛查 Mapper。
 */
@Mapper
public interface BizNutritionScreenMapper extends BaseMapper<BizNutritionScreen> {

    /**
     * 列表与历史共用同一投影，避免两套口径
     */
    String PROJECTION = """
            <script>
            SELECT s.*,
                   a.admission_no,
                   a.admit_status,
                   p.gender,
                   p.age,
                   CASE s.screen_type WHEN 1 THEN 'NRS2002 营养风险筛查' WHEN 2 THEN 'PG-SGA 主观整体评估'
                                      WHEN 3 THEN 'MNA 老年微型营养评估' ELSE '未知' END AS screen_type_text,
                   CASE s.risk_flag WHEN 0 THEN '无营养风险' WHEN 1 THEN '有营养风险' ELSE '未知' END AS risk_flag_text,
                   CASE s.screen_source WHEN 1 THEN '入院48小时内' WHEN 2 THEN '病情变化复筛'
                                        WHEN 3 THEN '术后复筛' WHEN 4 THEN '定期复筛' ELSE '未知' END AS screen_source_text,
                   CASE WHEN s.next_screen_date IS NOT NULL AND s.next_screen_date &lt;= CURDATE() THEN 1 ELSE 0 END AS re_screen_due
              FROM biz_nutrition_screen s
              LEFT JOIN biz_admission a ON a.admission_id = s.admission_id AND a.del_flag = 0
              LEFT JOIN biz_patient p ON p.id = s.patient_id AND p.del_flag = 0
             WHERE s.del_flag = 0
            """;

    @Select(PROJECTION + """
            AND (#{q.admissionId} IS NULL OR s.admission_id = #{q.admissionId})
            AND (#{q.patientId} IS NULL OR s.patient_id = #{q.patientId})
            AND (#{q.screenType} IS NULL OR s.screen_type = #{q.screenType})
            AND (#{q.riskFlag} IS NULL OR s.risk_flag = #{q.riskFlag})
            AND (#{q.screenSource} IS NULL OR s.screen_source = #{q.screenSource})
            AND (#{q.deptId} IS NULL OR s.dept_id = #{q.deptId})
            AND (#{q.wardId} IS NULL OR s.ward_id = #{q.wardId})
            AND (#{q.admitStatus} IS NULL OR a.admit_status = #{q.admitStatus})
            AND (#{q.beginDate} IS NULL OR s.screen_time >= #{q.beginDate})
            AND (#{q.endDate} IS NULL OR s.screen_time &lt; DATE_ADD(#{q.endDate}, INTERVAL 1 DAY))
            <if test="q.scopeDeptIds != null and q.scopeDeptIds.size() > 0">
              AND s.dept_id IN
              <foreach collection="q.scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
            </if>
            <if test="q.dueOnly != null and q.dueOnly == 1">
              AND s.next_screen_date IS NOT NULL AND s.next_screen_date &lt;= CURDATE()
            </if>
            AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                 OR s.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                 OR s.patient_no LIKE CONCAT('%', #{q.keyword}, '%')
                 OR s.screen_no LIKE CONCAT('%', #{q.keyword}, '%')
                 OR a.admission_no LIKE CONCAT('%', #{q.keyword}, '%'))
             ORDER BY s.screen_time DESC, s.id DESC
             </script>
            """)
    IPage<NutritionScreenVO> selectScreenPage(Page<NutritionScreenVO> page,
                                              @Param("q") NutritionScreenQueryPageDTO query);

    /**
     * 某次住院的筛查历史（膳食方案弹框里看"这个人的筛查轨迹"）
     */
    @Select(PROJECTION + """
            AND s.admission_id = #{admissionId}
             ORDER BY s.screen_time DESC, s.id DESC
             LIMIT 20
             </script>
            """)
    List<NutritionScreenVO> selectByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 单条详情（保存后回给出参用，与列表同一投影）
     */
    @Select(PROJECTION + """
            AND s.id = #{id}
             LIMIT 1
             </script>
            """)
    NutritionScreenVO selectVoById(@Param("id") Long id);

    /**
     * 单号前缀当日已用最大序号（NS+yyyyMMdd+4位）—— 取 MAX 不取 COUNT，删过一条也不会撞号
     */
    @Select("SELECT COALESCE(MAX(CAST(RIGHT(screen_no, 4) AS UNSIGNED)), 0) "
            + "FROM biz_nutrition_screen WHERE screen_no LIKE CONCAT(#{prefix}, '%')")
    long maxScreenSeq(@Param("prefix") String prefix);
}
