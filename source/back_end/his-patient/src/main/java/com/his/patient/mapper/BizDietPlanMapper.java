package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.patient.dto.DietPlanQueryPageDTO;
import com.his.patient.entity.BizDietPlan;
import com.his.patient.vo.DietPlanVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 膳食方案 Mapper。
 *
 * <p><b>物理删除</b>：唯一键 {@code uk_diet_plan_order(order_id)} 不含 del_flag，
 * 软删行会占住这条医嘱的键位 —— 医嘱重新校对（或撤停后重开）时派生第二次 insert 直接
 * {@code Duplicate entry}，现象是"停嘱再开就 500"。删除一律走 {@link #purgeById}。
 *
 * <p>排序 {@code confirm_status ASC, start_time DESC, id DESC}：待接收的排最前，
 * 营养科一进门看到的就是今天要接的单。
 */
@Mapper
public interface BizDietPlanMapper extends BaseMapper<BizDietPlan> {

    String PROJECTION = """
            <script>
            SELECT v.*,
                   a.admission_no,
                   a.admit_status,
                   p.gender,
                   p.age,
                   CASE v.source WHEN 1 THEN '医嘱派生' WHEN 2 THEN '营养师登记' ELSE '未知' END AS source_text,
                   CASE v.diet_category WHEN 1 THEN '基本饮食' WHEN 2 THEN '治疗饮食'
                                        WHEN 3 THEN '诊断试验饮食' WHEN 4 THEN '营养支持' ELSE '未知' END AS diet_category_text,
                   CASE v.route WHEN 1 THEN '口服' WHEN 2 THEN '管饲' WHEN 3 THEN '静脉（肠外）' ELSE '未知' END AS route_text,
                   CASE v.route WHEN 1 THEN 1 ELSE 0 END AS needs_meal,
                   CASE v.plan_status WHEN 1 THEN '执行中' WHEN 2 THEN '已停止' WHEN 3 THEN '已作废' ELSE '未知' END AS plan_status_text,
                   CASE v.confirm_status WHEN 0 THEN '待接收' WHEN 1 THEN '已接收' WHEN 2 THEN '已退回' ELSE '未知' END AS confirm_status_text,
                   (SELECT COUNT(*) FROM biz_meal_order m
                     WHERE m.del_flag = 0 AND m.diet_plan_id = v.id AND m.meal_date = CURDATE()) AS today_meal_count
              FROM biz_diet_plan v
              LEFT JOIN biz_admission a ON a.admission_id = v.admission_id AND a.del_flag = 0
              LEFT JOIN biz_patient p ON p.id = v.patient_id AND p.del_flag = 0
            """;

    @Delete("DELETE FROM biz_diet_plan WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    /**
     * 单号前缀当日已用最大序号（DP+yyyyMMdd+4位）
     */
    @Select("SELECT COALESCE(MAX(CAST(RIGHT(diet_no, 4) AS UNSIGNED)), 0) "
            + "FROM biz_diet_plan WHERE diet_no LIKE CONCAT(#{prefix}, '%')")
    long maxDietSeq(@Param("prefix") String prefix);

    @Select(PROJECTION + """
             WHERE v.del_flag = 0
               AND (#{q.admissionId} IS NULL OR v.admission_id = #{q.admissionId})
               AND (#{q.deptId} IS NULL OR v.dept_id = #{q.deptId})
               AND (#{q.wardId} IS NULL OR v.ward_id = #{q.wardId})
               AND (#{q.source} IS NULL OR v.source = #{q.source})
               AND (#{q.dietCategory} IS NULL OR v.diet_category = #{q.dietCategory})
               AND (#{q.route} IS NULL OR v.route = #{q.route})
               AND (#{q.dietCode} IS NULL OR #{q.dietCode} = '' OR v.diet_code = #{q.dietCode})
               AND (#{q.planStatus} IS NULL OR v.plan_status = #{q.planStatus})
               AND (#{q.confirmStatus} IS NULL OR v.confirm_status = #{q.confirmStatus})
               AND (#{q.beginDate} IS NULL OR v.start_time >= #{q.beginDate})
               AND (#{q.endDate} IS NULL OR v.start_time &lt; DATE_ADD(#{q.endDate}, INTERVAL 1 DAY))
               <if test="q.scopeDeptIds != null and q.scopeDeptIds.size() > 0">
                 AND v.dept_id IN
                 <foreach collection="q.scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
               AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                    OR v.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                    OR v.patient_no LIKE CONCAT('%', #{q.keyword}, '%')
                    OR v.diet_no LIKE CONCAT('%', #{q.keyword}, '%')
                    OR v.order_no LIKE CONCAT('%', #{q.keyword}, '%')
                    OR a.admission_no LIKE CONCAT('%', #{q.keyword}, '%'))
             ORDER BY v.confirm_status ASC, v.start_time DESC, v.id DESC
             </script>
            """)
    IPage<DietPlanVO> selectPlanPage(Page<DietPlanVO> page, @Param("q") DietPlanQueryPageDTO query);

    /**
     * 某次住院的方案（含已停止/作废，营养科看历史）
     */
    @Select(PROJECTION + """
             WHERE v.del_flag = 0 AND v.admission_id = #{admissionId}
             ORDER BY v.plan_status ASC, v.start_time DESC, v.id DESC
             </script>
            """)
    List<DietPlanVO> selectByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 单条详情（保存/接收后回给出参用）
     */
    @Select(PROJECTION + """
             WHERE v.del_flag = 0 AND v.id = #{id}
             LIMIT 1
             </script>
            """)
    DietPlanVO selectVoById(@Param("id") Long id);
}
