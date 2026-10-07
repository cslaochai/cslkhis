package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.emr.entity.BizSurveyAnswer;
import com.his.emr.vo.SurveyAnswerVO;
import com.his.emr.vo.SurveyDayTrendVO;
import com.his.emr.vo.SurveyDeptScoreVO;
import com.his.emr.vo.SurveyDimensionStatVO;
import com.his.emr.vo.SurveyNpsStatVO;
import com.his.emr.vo.SurveyOverallStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 满意度答卷 Mapper（列表 + 看板聚合）。
 *
 * <p>所有聚合只认 {@code answer_status = 1}（有效卷）：作废卷必须留在表里做审计，
 * 但绝不能继续混在均分里 —— 混了之后「谁把某个月的分数拉高了」无从解释。
 */
@Mapper
public interface BizSurveyAnswerMapper extends BaseMapper<BizSurveyAnswer> {
    @Select("""
            <script>
            SELECT a.*, t.template_name AS templateName
              FROM biz_survey_answer a
              LEFT JOIN biz_survey_template t ON t.id = a.template_id
             WHERE a.del_flag = 0
               <if test="keyword != null and keyword != ''">
                 AND (a.answer_no LIKE CONCAT('%', #{keyword}, '%')
                   OR a.patient_name LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="patientId != null"> AND a.patient_id = #{patientId}</if>
               <if test="templateId != null"> AND a.template_id = #{templateId}</if>
               <if test="scene != null"> AND a.scene = #{scene}</if>
               <if test="answerStatus != null"> AND a.answer_status = #{answerStatus}</if>
               <if test="fillSource != null"> AND a.fill_source = #{fillSource}</if>
               <if test="lowScoreOnly != null and lowScoreOnly == true">
                 AND (a.score_100 &lt; 60 OR EXISTS (SELECT 1 FROM biz_survey_answer_item i
                        WHERE i.answer_id = a.id AND i.del_flag = 0
                          AND i.question_type = 1 AND i.score &lt;= 2))
               </if>
               <if test="dateFrom != null and dateFrom != ''"> AND a.fill_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND a.fill_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND a.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
             ORDER BY a.id DESC
            </script>
            """)
    List<SurveyAnswerVO> selectAnswerPage(IPage<SurveyAnswerVO> page,
                                          @Param("keyword") String keyword,
                                          @Param("patientId") Long patientId,
                                          @Param("templateId") Long templateId,
                                          @Param("scene") Integer scene,
                                          @Param("answerStatus") Integer answerStatus,
                                          @Param("fillSource") Integer fillSource,
                                          @Param("lowScoreOnly") Boolean lowScoreOnly,
                                          @Param("dateFrom") String dateFrom,
                                          @Param("dateTo") String dateTo,
                                          @Param("scopeDeptIds") List<Long> scopeDeptIds);

    @Select("SELECT a.*, t.template_name AS templateName FROM biz_survey_answer a "
            + "LEFT JOIN biz_survey_template t ON t.id = a.template_id "
            + "WHERE a.id = #{id} AND a.del_flag = 0")
    SurveyAnswerVO selectAnswerById(@Param("id") Long id);

    /**
     * 总览：有效卷数、均分、百分制、满意率、低分数、已转投诉数、作废数
     */
    @Select("""
            <script>
            SELECT COUNT(CASE WHEN a.answer_status = 1 THEN 1 END) AS total,
                   ROUND(AVG(CASE WHEN a.answer_status = 1 THEN a.avg_score END), 2)  AS avgScore,
                   ROUND(AVG(CASE WHEN a.answer_status = 1 THEN a.score_100 END), 2)  AS avgScore100,
                   COUNT(CASE WHEN a.answer_status = 1 AND a.avg_score >= 4 THEN 1 END) AS satisfied,
                   COUNT(CASE WHEN a.answer_status = 1 AND (a.score_100 &lt; 60
                        OR EXISTS (SELECT 1 FROM biz_survey_answer_item i
                                    WHERE i.answer_id = a.id AND i.del_flag = 0
                                      AND i.question_type = 1 AND i.score &lt;= 2)) THEN 1 END) AS lowScore,
                   COUNT(CASE WHEN a.answer_status = 1 AND a.dispute_case_id IS NOT NULL THEN 1 END) AS disputed,
                   COUNT(CASE WHEN a.answer_status = 2 THEN 1 END) AS voided
              FROM biz_survey_answer a
             WHERE a.del_flag = 0
               <if test="templateId != null"> AND a.template_id = #{templateId}</if>
               <if test="scene != null"> AND a.scene = #{scene}</if>
               <if test="dateFrom != null and dateFrom != ''"> AND a.fill_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND a.fill_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND a.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
            </script>
            """)
    SurveyOverallStatVO statOverall(@Param("templateId") Long templateId,
                                    @Param("scene") Integer scene,
                                    @Param("dateFrom") String dateFrom,
                                    @Param("dateTo") String dateTo,
                                    @Param("scopeDeptIds") List<Long> scopeDeptIds);

    /**
     * NPS 分档（推荐者 9-10 / 中立 7-8 / 贬损者 0-6），只认有效卷
     */
    @Select("""
            <script>
            SELECT COUNT(a.nps) AS rated,
                   COUNT(CASE WHEN a.nps >= 9 THEN 1 END) AS promoter,
                   COUNT(CASE WHEN a.nps BETWEEN 7 AND 8 THEN 1 END) AS passive,
                   COUNT(CASE WHEN a.nps &lt;= 6 THEN 1 END) AS detractor
              FROM biz_survey_answer a
             WHERE a.del_flag = 0 AND a.answer_status = 1 AND a.nps IS NOT NULL
               <if test="templateId != null"> AND a.template_id = #{templateId}</if>
               <if test="dateFrom != null and dateFrom != ''"> AND a.fill_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND a.fill_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND a.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
            </script>
            """)
    SurveyNpsStatVO statNps(@Param("templateId") Long templateId,
                             @Param("dateFrom") String dateFrom,
                             @Param("dateTo") String dateTo,
                             @Param("scopeDeptIds") List<Long> scopeDeptIds);

    /**
     * 维度均分（升序 = 短板在前）。
     *
     * <p>用 answer_item 上的**维度快照**，不是 item 表的当前维度：模板改维度后，
     * 历史卷必须还记在它当时的维度下，否则报表会把去年的分数挪到今天的维度上。
     */
    @Select("""
            <script>
            SELECT i.dimension AS dimension, COUNT(*) AS cnt, ROUND(AVG(i.score), 2) AS avgScore
              FROM biz_survey_answer_item i
              JOIN biz_survey_answer a ON a.id = i.answer_id AND a.del_flag = 0 AND a.answer_status = 1
             WHERE i.del_flag = 0 AND i.question_type = 1 AND i.score IS NOT NULL
               <if test="templateId != null"> AND i.template_id = #{templateId}</if>
               <if test="dateFrom != null and dateFrom != ''"> AND a.fill_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND a.fill_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND a.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
             GROUP BY i.dimension ORDER BY avgScore ASC
            </script>
            """)
    List<SurveyDimensionStatVO> statByDimension(@Param("templateId") Long templateId,
                                                    @Param("dateFrom") String dateFrom,
                                                    @Param("dateTo") String dateTo,
                                                    @Param("scopeDeptIds") List<Long> scopeDeptIds);

    /**
     * 科室短板 TOP10（百分制均分升序 —— 评审要的是短板榜，不是光荣榜）。
     *
     * <p>科室名以科室为准、快照兜底，理由同随访看板：撤科或快照缺失时
     * 不要把「有科室但查不到名字」和「根本没科室」混成一行。
     */
    @Select("""
            <script>
            SELECT COALESCE(a.dept_id, 0) AS deptId,
                   COALESCE(NULLIF(dep.dept_name, ''), NULLIF(a.dept_name, ''),
                            CASE WHEN a.dept_id IS NULL THEN '未指定科室' ELSE CONCAT('科室#', a.dept_id) END) AS deptName,
                   COUNT(*) AS cnt, ROUND(AVG(a.score_100), 2) AS avgScore100
              FROM biz_survey_answer a
              LEFT JOIN sys_department dep ON dep.id = a.dept_id AND dep.del_flag = 0
             WHERE a.del_flag = 0 AND a.answer_status = 1
               <if test="templateId != null"> AND a.template_id = #{templateId}</if>
               <if test="dateFrom != null and dateFrom != ''"> AND a.fill_time &gt;= #{dateFrom}</if>
               <if test="dateTo != null and dateTo != ''"> AND a.fill_time &lt;= CONCAT(#{dateTo}, ' 23:59:59')</if>
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND a.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
             GROUP BY COALESCE(a.dept_id, 0), deptName
             ORDER BY avgScore100 ASC, cnt DESC LIMIT 10
            </script>
            """)
    List<SurveyDeptScoreVO> statByDeptBottom(@Param("templateId") Long templateId,
                                                  @Param("dateFrom") String dateFrom,
                                                  @Param("dateTo") String dateTo,
                                                  @Param("scopeDeptIds") List<Long> scopeDeptIds);

    /**
     * 近 30 日趋势（按提交日聚合，只认有效卷）
     */
    @Select("""
            <script>
            SELECT DATE_FORMAT(a.fill_time, '%Y-%m-%d') AS statDate, COUNT(*) AS cnt,
                   ROUND(AVG(a.score_100), 2) AS avgScore100
              FROM biz_survey_answer a
             WHERE a.del_flag = 0 AND a.answer_status = 1
               AND a.fill_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
               <if test="templateId != null"> AND a.template_id = #{templateId}</if>
               <if test="scopeDeptIds != null and scopeDeptIds.size() > 0">
                 AND a.dept_id IN
                 <foreach collection="scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
               </if>
             GROUP BY DATE_FORMAT(a.fill_time, '%Y-%m-%d') ORDER BY statDate ASC
            </script>
            """)
    List<SurveyDayTrendVO> statByDay(@Param("templateId") Long templateId,
                                     @Param("scopeDeptIds") List<Long> scopeDeptIds);
}
