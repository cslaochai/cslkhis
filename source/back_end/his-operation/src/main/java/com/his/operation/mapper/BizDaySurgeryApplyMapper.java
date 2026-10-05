package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.entity.BizDaySurgeryApply;
import com.his.operation.vo.DaySurgeryApplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 日间手术登记单 Mapper。
 *
 * <p>「超期」在 SQL 里判：术后观察滞留小时数 &gt; 该术式 max_stay_hours。
 * 不落库 —— 落库就会停在错误的值上，翻页时又不会更新。
 */
@Mapper
public interface BizDaySurgeryApplyMapper extends BaseMapper<BizDaySurgeryApply> {

    @Select("""
            <script>
            SELECT a.* FROM biz_day_surgery_apply a
             WHERE a.del_flag = 0
               <if test="keyword != null and keyword != ''">
                 AND (a.apply_no LIKE CONCAT('%', #{keyword}, '%')
                   OR a.patient_name LIKE CONCAT('%', #{keyword}, '%')
                   OR a.item_name LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="status != null"> AND a.status = #{status}</if>
               <if test="itemId != null"> AND a.item_id = #{itemId}</if>
               <if test="deptId != null"> AND a.dept_id = #{deptId}</if>
               <if test="openOnly != null and openOnly == true"> AND a.status IN (1, 2, 3, 4)</if>
               <if test="overdueOnly != null and overdueOnly == true">
                 AND a.status = 4 AND a.surgery_end_time IS NOT NULL
                 AND TIMESTAMPDIFF(HOUR, a.surgery_end_time, NOW()) &gt; a.max_stay_hours
               </if>
               <if test="dateFrom != null and dateFrom != ''"> AND a.plan_surgery_date &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND a.plan_surgery_date &lt;= #{dateTo}</if>
             ORDER BY a.status ASC, a.id DESC
            </script>
            """)
    List<DaySurgeryApplyVO> selectApplyPage(IPage<DaySurgeryApplyVO> page,
                                            @Param("keyword") String keyword,
                                            @Param("status") Integer status,
                                            @Param("itemId") Long itemId,
                                            @Param("deptId") Long deptId,
                                            @Param("openOnly") Boolean openOnly,
                                            @Param("overdueOnly") Boolean overdueOnly,
                                            @Param("dateFrom") String dateFrom,
                                            @Param("dateTo") String dateTo);

    @Select("SELECT a.* FROM biz_day_surgery_apply a WHERE a.id = #{id} AND a.del_flag = 0")
    DaySurgeryApplyVO selectApplyById(@Param("id") Long id);

    @Select("SELECT d.dept_name FROM sys_department d WHERE d.id = #{deptId} AND d.del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    /**
     * 转住院号是否存在（入院记录同模块，但不落实体依赖，用裸 SQL 计数）
     */
    @Select("SELECT COUNT(*) FROM biz_admission m WHERE m.admission_id = #{admissionId} AND m.del_flag = 0")
    int countAdmission(@Param("admissionId") Long admissionId);

    // 统计

    @Select("SELECT a.status AS k, COUNT(*) AS c FROM biz_day_surgery_apply a WHERE a.del_flag = 0 GROUP BY a.status")
    List<Map<String, Object>> countByStatus();

    /**
     * 术后滞留超期：术后观察中且滞留小时数超过该术式上限
     */
    @Select("SELECT COUNT(*) FROM biz_day_surgery_apply a WHERE a.del_flag = 0 AND a.status = 4 " +
            "  AND a.surgery_end_time IS NOT NULL " +
            "  AND TIMESTAMPDIFF(HOUR, a.surgery_end_time, NOW()) > a.max_stay_hours")
    long countOverdue();

    /**
     * 应随访未随访：已离院（含转住院）超 24h 且随访次数为 0
     */
    @Select("SELECT COUNT(*) FROM biz_day_surgery_apply a WHERE a.del_flag = 0 AND a.status IN (5, 7) " +
            "  AND a.discharge_time IS NOT NULL AND a.follow_count = 0 " +
            "  AND TIMESTAMPDIFF(HOUR, a.discharge_time, NOW()) > 24")
    long countFollowOverdue();

    @Select("SELECT COUNT(*) FROM biz_day_surgery_apply a WHERE a.del_flag = 0 AND a.leave_type = 3")
    long countReadmit();

    @Select("SELECT COALESCE(ROUND(SUM(CASE WHEN a.leave_type = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1), 0) AS rate " +
            "  FROM biz_day_surgery_apply a WHERE a.del_flag = 0 AND a.status = 5")
    java.math.BigDecimal onTimeLeaveRate();

    @Select("SELECT a.item_id AS i, COALESCE(a.item_name, '未知术式') AS n, COUNT(*) AS c " +
            "  FROM biz_day_surgery_apply a WHERE a.del_flag = 0 " +
            " GROUP BY a.item_id, COALESCE(a.item_name, '未知术式') " +
            " ORDER BY c DESC, i ASC LIMIT 10")
    List<Map<String, Object>> countByItemTop();
}
