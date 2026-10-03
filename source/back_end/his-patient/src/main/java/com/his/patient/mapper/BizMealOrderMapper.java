package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.patient.dto.MealOrderQueryPageDTO;
import com.his.patient.entity.BizMealOrder;
import com.his.patient.vo.MealOrderVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 订餐配送 Mapper。
 *
 * <p><b>重生成必须物理删</b>：唯一键 {@code uk_meal_order(admission_id, meal_date, meal_type)} 不含
 * del_flag，软删旧行再插同一天同一餐次会 {@code Duplicate entry}。而且"删了再插"只允许发生在
 * <b>还没配送</b>（0-待配餐 / 1-已配餐）的行上 —— 已配送、已签收的餐是既成事实，
 * 一次点击就把它抹掉，等于让这个人当天没饭吃且查不到原因。
 */
@Mapper
public interface BizMealOrderMapper extends BaseMapper<BizMealOrder> {

    @Delete("DELETE FROM biz_meal_order WHERE id = #{id}")
    int purgeById(@Param("id") Long id);

    /** 覆盖重生成用：物理清掉指定日期、指定人范围内「还没配送」的餐行 */
    @Delete("""
            <script>
            DELETE FROM biz_meal_order
             WHERE meal_date = #{mealDate}
               AND deliver_status IN (0, 1)
               AND admission_id IN
              <foreach collection="admissionIds" item="ad" open="(" separator="," close=")">#{ad}</foreach>
            </script>
            """)
    int purgePendingByDate(@Param("mealDate") LocalDate mealDate,
                           @Param("admissionIds") List<Long> admissionIds);

    /** 单号前缀当日已用最大序号（MO+yyyyMMdd+4位） */
    @Select("SELECT COALESCE(MAX(CAST(RIGHT(meal_no, 4) AS UNSIGNED)), 0) "
            + "FROM biz_meal_order WHERE meal_no LIKE CONCAT(#{prefix}, '%')")
    long maxMealSeq(@Param("prefix") String prefix);

    String PROJECTION = """
            <script>
            SELECT m.*,
                   a.admission_no,
                   CASE m.meal_type WHEN 1 THEN '早餐' WHEN 2 THEN '午餐' WHEN 3 THEN '晚餐'
                                    WHEN 4 THEN '加餐' ELSE '未知' END AS meal_type_text,
                   CASE m.deliver_status WHEN 0 THEN '待配餐' WHEN 1 THEN '已配餐' WHEN 2 THEN '已配送'
                                         WHEN 3 THEN '已签收' WHEN 4 THEN '已取消' ELSE '未知' END AS deliver_status_text,
                   CASE m.source WHEN 1 THEN '方案生成' WHEN 2 THEN '手工加订' ELSE '未知' END AS source_text
              FROM biz_meal_order m
              LEFT JOIN biz_admission a ON a.admission_id = m.admission_id AND a.del_flag = 0
            """;

    @Select(PROJECTION + """
             WHERE m.del_flag = 0
               AND (#{q.mealDate} IS NULL OR m.meal_date = #{q.mealDate})
               AND (#{q.beginDate} IS NULL OR m.meal_date >= #{q.beginDate})
               AND (#{q.endDate} IS NULL OR m.meal_date &lt;= #{q.endDate})
               AND (#{q.wardId} IS NULL OR m.ward_id = #{q.wardId})
               AND (#{q.deptId} IS NULL OR m.dept_id = #{q.deptId})
               AND (#{q.admissionId} IS NULL OR m.admission_id = #{q.admissionId})
               AND (#{q.dietPlanId} IS NULL OR m.diet_plan_id = #{q.dietPlanId})
               AND (#{q.deliverStatus} IS NULL OR m.deliver_status = #{q.deliverStatus})
               AND (#{q.mealType} IS NULL OR m.meal_type = #{q.mealType})
               AND (#{q.dietCode} IS NULL OR #{q.dietCode} = '' OR m.diet_code = #{q.dietCode})
               <if test="q.scopeDeptIds != null and q.scopeDeptIds.size() > 0">
                 AND m.dept_id IN
                 <foreach collection="q.scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
               AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                    OR m.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                    OR m.patient_no LIKE CONCAT('%', #{q.keyword}, '%')
                    OR m.meal_no LIKE CONCAT('%', #{q.keyword}, '%')
                    OR a.admission_no LIKE CONCAT('%', #{q.keyword}, '%'))
             ORDER BY m.meal_date DESC, m.meal_type ASC, m.ward_id ASC, m.id DESC
             </script>
            """)
    IPage<MealOrderVO> selectMealPage(Page<MealOrderVO> page, @Param("q") MealOrderQueryPageDTO query);

    /** 某膳食方案名下的餐行（方案详情看"这个人订了哪些餐"） */
    @Select(PROJECTION + """
             WHERE m.del_flag = 0 AND m.diet_plan_id = #{dietPlanId}
             ORDER BY m.meal_date DESC, m.meal_type ASC
             LIMIT 60
             </script>
            """)
    List<MealOrderVO> selectByPlan(@Param("dietPlanId") Long dietPlanId);

    /** 单条详情（状态推进后回给出参用） */
    @Select(PROJECTION + """
             WHERE m.del_flag = 0 AND m.id = #{id}
             LIMIT 1
             </script>
            """)
    MealOrderVO selectVoById(@Param("id") Long id);
}
