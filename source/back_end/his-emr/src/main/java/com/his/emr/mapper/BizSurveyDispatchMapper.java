package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.emr.entity.BizSurveyDispatch;
import com.his.emr.vo.SurveyChannelStatVO;
import com.his.emr.vo.SurveyDispatchCountVO;
import com.his.emr.vo.SurveyDispatchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 满意度发放/回收台账 Mapper。
 *
 * <p>裸 SQL 一律自带 {@code del_flag = 0}（@Select 不走 MP 的逻辑删除拦截），
 * 日期上界一律补 ' 23:59:59'（DATETIME 与 'yyyy-MM-dd' 直接比会把当天全滤掉）。
 */
@Mapper
public interface BizSurveyDispatchMapper extends BaseMapper<BizSurveyDispatch> {

    /**
     * 发放/回收分页。
     *
     * <p>{@code overdue} 在 SQL 里现算（状态<3 且已过 expire_time）：
     * 不建定时任务去翻 dispatch_status，否则会出现「日期已过、状态还没刷」的漂移窗口。
     */
    @Select("""
            <script>
            SELECT d.*, CASE WHEN d.dispatch_status &lt; 3 AND d.expire_time IS NOT NULL
                              AND d.expire_time &lt; NOW() THEN 1 ELSE 0 END AS overdue
              FROM biz_survey_dispatch d
             WHERE d.del_flag = 0
               <if test="keyword != null and keyword != ''">
                 AND (d.dispatch_no LIKE CONCAT('%', #{keyword}, '%')
                   OR d.patient_name LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="patientId != null"> AND d.patient_id = #{patientId}</if>
               <if test="sourceType != null"> AND d.source_type = #{sourceType}</if>
               <if test="dispatchStatus != null"> AND d.dispatch_status = #{dispatchStatus}</if>
               <if test="channel != null"> AND d.channel = #{channel}</if>
               <if test="overdueOnly != null and overdueOnly == true">
                 AND d.dispatch_status &lt; 3 AND d.expire_time IS NOT NULL AND d.expire_time &lt; NOW()
               </if>
               <if test="dateFrom != null and dateFrom != ''"> AND d.create_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND d.create_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND d.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
             ORDER BY d.id DESC
            </script>
            """)
    List<SurveyDispatchVO> selectDispatchPage(IPage<SurveyDispatchVO> page,
                                              @Param("keyword") String keyword,
                                              @Param("patientId") Long patientId,
                                              @Param("sourceType") Integer sourceType,
                                              @Param("dispatchStatus") Integer dispatchStatus,
                                              @Param("channel") Integer channel,
                                              @Param("overdueOnly") Boolean overdueOnly,
                                              @Param("dateFrom") String dateFrom,
                                              @Param("dateTo") String dateTo,
                                              @Param("scopeDeptIds") List<Long> scopeDeptIds);

    @Select("SELECT d.* FROM biz_survey_dispatch d WHERE d.id = #{id} AND d.del_flag = 0")
    BizSurveyDispatch selectDispatchById(@Param("id") Long id);

    /**
     * 同一次来源是否已发放过（幂等：撞 uk_survey_dispatch_source 之前先问一次，返回既有单）
     */
    @Select("SELECT * FROM biz_survey_dispatch WHERE del_flag = 0 "
            + "AND source_type = #{sourceType} AND source_id = #{sourceId} AND template_id = #{templateId} LIMIT 1")
    BizSurveyDispatch selectBySource(@Param("sourceType") Integer sourceType,
                                     @Param("sourceId") Long sourceId,
                                     @Param("templateId") Long templateId);

    /**
     * 回收状态分布（key=dispatch_status，c=条数）
     */
    @Select("""
            <script>
            SELECT d.dispatch_status AS k, COUNT(*) AS c
              FROM biz_survey_dispatch d
             WHERE d.del_flag = 0
               <if test="templateId != null"> AND d.template_id = #{templateId}</if>
               <if test="sourceType != null"> AND d.source_type = #{sourceType}</if>
               <if test="dateFrom != null and dateFrom != ''"> AND d.create_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND d.create_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND d.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
             GROUP BY d.dispatch_status
            </script>
            """)
    List<SurveyDispatchCountVO> countByStatus(@Param("templateId") Long templateId,
                                             @Param("sourceType") Integer sourceType,
                                             @Param("dateFrom") String dateFrom,
                                             @Param("dateTo") String dateTo,
                                             @Param("scopeDeptIds") List<Long> scopeDeptIds);

    /**
     * 超截止仍未回收的条数（看板「待催办」）
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_survey_dispatch d
             WHERE d.del_flag = 0 AND d.dispatch_status &lt; 3
               AND d.expire_time IS NOT NULL AND d.expire_time &lt; NOW()
               <if test="templateId != null"> AND d.template_id = #{templateId}</if>
               <if test="sourceType != null"> AND d.source_type = #{sourceType}</if>
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND d.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
            </script>
            """)
    Long countOverdue(@Param("templateId") Long templateId,
                      @Param("sourceType") Integer sourceType,
                      @Param("scopeDeptIds") List<Long> scopeDeptIds);

    /**
     * 按渠道的发放/回收（把「发出去永远收不回」的渠道显式暴露出来）
     */
    @Select("""
            <script>
            SELECT d.channel AS k, COUNT(*) AS total,
                   SUM(CASE WHEN d.dispatch_status = 3 THEN 1 ELSE 0 END) AS recycled
              FROM biz_survey_dispatch d
             WHERE d.del_flag = 0
               <if test="templateId != null"> AND d.template_id = #{templateId}</if>
               <if test="dateFrom != null and dateFrom != ''"> AND d.create_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND d.create_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND d.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
             GROUP BY d.channel ORDER BY d.channel
            </script>
            """)
    List<SurveyChannelStatVO> countByChannel(@Param("templateId") Long templateId,
                                              @Param("dateFrom") String dateFrom,
                                              @Param("dateTo") String dateTo,
                                              @Param("scopeDeptIds") List<Long> scopeDeptIds);
}
