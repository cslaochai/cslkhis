package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.emr.dto.QcCandidateQueryPageDTO;
import com.his.emr.dto.QcQueryPageDTO;
import com.his.emr.entity.BizQualityControl;
import com.his.emr.vo.QcIssueVO;
import com.his.emr.vo.BizQualityControlVO;
import com.his.emr.vo.QcCandidateVO;
import com.his.emr.vo.QcOverviewVO;
import com.his.emr.vo.QcRuleMetricVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 质控检查记录Mapper
 */
@Mapper
public interface BizQualityControlMapper extends BaseMapper<BizQualityControl> {

    /**
     * 质控单查询的公共列。
     *
     * <p><b>为什么要提成常量</b>：分页查询与"按 ID 读单"必须给出**完全同形**的结果 ——
     * 否则「刚执行完质控弹出的详情」与「在列表里点开的同一张单」字段不一样，
     * 界面上会出现"执行完看不到患者姓名，刷新一下才有"这种最难查的缺陷。
     * 文本块与 String 常量拼接在注解里是编译期常量，可以这么写。
     */
    String QC_VO_COLUMNS = """
            q.id, q.qc_no, q.record_id, q.record_source, q.patient_id, q.qc_type, q.qc_content,
            q.qc_result, q.error_count, q.error_detail, q.score, q.severity_max,
            q.qc_status, q.qc_by, q.qc_time, q.create_by, q.create_time,
            q.update_by, q.update_time, q.del_flag, q.remark,
            COALESCE(m.record_no, r.record_no)         AS record_no,
            COALESCE(m.patient_no, r.patient_no)       AS patient_no,
            COALESCE(m.patient_name, r.patient_name)   AS patient_name,
            COALESCE(m.dept_name, r.dept_name)         AS dept_name,
            COALESCE(m.doctor_name, r.doctor_name)     AS doctor_name,
            r.record_type                              AS record_type,
            COALESCE(m.record_status, r.record_status) AS record_status
            """;

    /**
     * 按记录来源决定 JOIN 哪张病历表 ——
     * 这正是新增 record_source 要解决的问题：以前 record_id 是个不知道 JOIN 谁的裸 ID。
     *
     * <p>两个 LEFT JOIN 都带 {@code del_flag = 0}：病历被删了不该让质控单从列表里消失，
     * 只是它的病历号显示为空。
     */
    String QC_VO_JOINS = """
            FROM biz_quality_control q
            LEFT JOIN biz_medical_record   m ON q.record_source = 'OUTPATIENT' AND m.id = q.record_id AND m.del_flag = 0
            LEFT JOIN biz_inpatient_record r ON q.record_source = 'INPATIENT'  AND r.id = q.record_id AND r.del_flag = 0
            """;

    /**
     * 质控单分页
     *
     * <p>deptIds 是 DeptScopeProvider 收口后的科室集合（见 QualityControlServiceImpl#scopedDeptIds）：
     * null = 当前角色不限科室（全院）；非 null = 只能看集合内科室（按病历归属科室收口，
     * 质控单本身无科室列，取门诊/住院文书两表的 COALESCE）。
     */
    @Select("SELECT " + QC_VO_COLUMNS + QC_VO_JOINS + """
            WHERE q.del_flag = 0
              AND (#{q.patientId} IS NULL OR q.patient_id = #{q.patientId})
              AND (#{q.recordSource} IS NULL OR q.record_source = #{q.recordSource})
              AND (#{q.qcType} IS NULL OR q.qc_type = #{q.qcType})
              AND (#{q.qcStatus} IS NULL OR q.qc_status = #{q.qcStatus})
              AND (#{q.qcResult} IS NULL OR q.qc_result = #{q.qcResult})
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR q.qc_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.record_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.record_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.patient_name LIKE CONCAT('%', #{q.keyword}, '%'))
              <if test="deptIds != null"> AND COALESCE(m.dept_id, r.dept_id) IN
                <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach></if>
            ORDER BY q.qc_time DESC, q.id DESC
            """)
    IPage<BizQualityControlVO> selectQcPage(IPage<BizQualityControlVO> page, @Param("q") QcQueryPageDTO query,
                                            @Param("deptIds") List<Long> deptIds);

    /**
     * 按 ID 读单张质控单（与分页查询同形）。执行质控与查看详情都必须走它，
     * 不能拿刚落库的实体直接搬字段 —— 实体里没有跨表快照。
     */
    @Select("SELECT " + QC_VO_COLUMNS + QC_VO_JOINS + """
            WHERE q.del_flag = 0 AND q.id = #{qcId}
            """)
    BizQualityControlVO selectQcById(@Param("qcId") Long qcId);

    /**
     * 门诊病历候选（待质控工作台）；deptIds 为科室数据权限收口集合（null = 全院）
     */
    @Select("""
            <script>
            SELECT 'OUTPATIENT' AS record_source, m.id AS record_id, m.record_no, m.patient_id, m.patient_no,
                   m.patient_name, m.gender, m.age, m.dept_name, m.doctor_name,
                   CAST(NULL AS SIGNED) AS record_type, m.record_status,
                   CAST(m.visit_date AS DATETIME) AS record_time,
                   q.id AS last_qc_id, q.qc_no AS last_qc_no, q.qc_time AS last_qc_time,
                   q.score AS last_score, q.severity_max AS last_severity_max, q.qc_result AS last_result
            FROM biz_medical_record m
            LEFT JOIN biz_quality_control q ON q.id = (
                SELECT q2.id FROM biz_quality_control q2
                WHERE q2.del_flag = 0 AND q2.record_source = 'OUTPATIENT' AND q2.record_id = m.id
                ORDER BY q2.qc_time DESC, q2.id DESC LIMIT 1)
            WHERE m.del_flag = 0
              AND (#{q.recordStatus} IS NULL OR m.record_status = #{q.recordStatus})
              AND (#{q.onlyUnQced} IS NULL OR #{q.onlyUnQced} = FALSE OR q.id IS NULL)
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR m.record_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR m.patient_name LIKE CONCAT('%', #{q.keyword}, '%'))
              <if test="deptIds != null"> AND m.dept_id IN
                <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach></if>
            ORDER BY m.visit_date DESC, m.id DESC
            </script>
            """)
    IPage<QcCandidateVO> selectOutpatientCandidatePage(IPage<QcCandidateVO> page,
                                                       @Param("q") QcCandidateQueryPageDTO query,
                                                       @Param("deptIds") List<Long> deptIds);

    /**
     * 住院文书候选（待质控工作台）；deptIds 为科室数据权限收口集合（null = 全院）
     */
    @Select("""
            <script>
            SELECT 'INPATIENT' AS record_source, r.id AS record_id, r.record_no, r.patient_id, r.patient_no,
                   r.patient_name, r.gender, r.age, r.dept_name, r.doctor_name,
                   r.record_type, r.record_status, r.record_time,
                   q.id AS last_qc_id, q.qc_no AS last_qc_no, q.qc_time AS last_qc_time,
                   q.score AS last_score, q.severity_max AS last_severity_max, q.qc_result AS last_result
            FROM biz_inpatient_record r
            LEFT JOIN biz_quality_control q ON q.id = (
                SELECT q2.id FROM biz_quality_control q2
                WHERE q2.del_flag = 0 AND q2.record_source = 'INPATIENT' AND q2.record_id = r.id
                ORDER BY q2.qc_time DESC, q2.id DESC LIMIT 1)
            WHERE r.del_flag = 0
              AND (#{q.recordStatus} IS NULL OR r.record_status = #{q.recordStatus})
              AND (#{q.recordType} IS NULL OR r.record_type = #{q.recordType})
              AND (#{q.onlyUnQced} IS NULL OR #{q.onlyUnQced} = FALSE OR q.id IS NULL)
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR r.record_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.patient_name LIKE CONCAT('%', #{q.keyword}, '%'))
              <if test="deptIds != null"> AND r.dept_id IN
                <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach></if>
            ORDER BY r.record_time DESC, r.id DESC
            </script>
            """)
    IPage<QcCandidateVO> selectInpatientCandidatePage(IPage<QcCandidateVO> page,
                                                      @Param("q") QcCandidateQueryPageDTO query,
                                                      @Param("deptIds") List<Long> deptIds);

    /**
     * 质控单概览。全部在一次查询里取，避免"总数"与"分项"来自两个时刻的两次查询。
     *
     * <p><b>质量指标（平均分 / 甲级率）只统计综合质控（{@code qc_type = 0}）</b>：
     * 完整性 / 规范性 / 逻辑性单独出单时，那个分数只代表**那一个维度**——
     * 实测这些单集中在 95~100 分（因为缺主诉这类否决项属完整性，单独跑逻辑性就看不到），
     * 把它们混进甲级率会把指标系统性抬高（库内实测 65.4% vs 64.2%，样本越大差得越多）。
     * 「单量类」指标（总数 / 待处理 / 通过 / 不通过 / 否决）仍然统计全部质控单 ——
     * 它们回答的是"有多少张单要处理"，跟分数口径无关。
     *
     * <p>{@code unscored_count} 反过来统计**全部**没得分的单：它要回答的是
     * "为什么有些行显示 —"，答案包括旧版质控与 AI 内涵质控，不限于综合质控。
     *
     * <p>deptIds 为科室数据权限收口集合（null = 全院）：概览与质控单列表同口径，
     * 按病历归属科室 EXISTS 过滤（质控单本身无科室列）。注意 script 块内 XML 转义，
     * 比较符写成 {@code 3 > severity_max} 而不是 {@code severity_max < 3}。
     */
    @Select("""
            <script>
            SELECT COUNT(*)                                                  AS total,
                   COALESCE(SUM(qc_status = 1), 0)                           AS pending_count,
                   COALESCE(SUM(qc_result = 0), 0)                           AS failed_count,
                   COALESCE(SUM(qc_result = 1), 0)                           AS passed_count,
                   COALESCE(SUM(severity_max = 3), 0)                        AS veto_count,
                   COALESCE(SUM(qc_type = 0 AND score IS NOT NULL), 0)       AS scored_count,
                   COALESCE(SUM(score IS NULL), 0)                           AS unscored_count,
                   ROUND(AVG(CASE WHEN qc_type = 0 THEN score END), 1)       AS avg_score,
                   COALESCE(SUM(qc_type = 0 AND score IS NOT NULL AND 3 > severity_max AND score >= 90), 0)                AS grade_a_count,
                   COALESCE(SUM(qc_type = 0 AND score IS NOT NULL AND 3 > severity_max AND score >= 75 AND 90 > score), 0) AS grade_b_count,
                   COALESCE(SUM(qc_type = 0 AND score IS NOT NULL AND (severity_max = 3 OR 75 > score)), 0)                AS grade_c_count,
                   MAX(qc_time)                                              AS last_qc_time
            FROM biz_quality_control
            WHERE del_flag = 0
            <if test="deptIds != null"> AND (
                 EXISTS (SELECT 1 FROM biz_medical_record m WHERE m.del_flag = 0
                         AND biz_quality_control.record_source = 'OUTPATIENT' AND m.id = biz_quality_control.record_id
                         AND m.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>)
              OR EXISTS (SELECT 1 FROM biz_inpatient_record r WHERE r.del_flag = 0
                         AND biz_quality_control.record_source = 'INPATIENT' AND r.id = biz_quality_control.record_id
                         AND r.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>)
            )</if>
            </script>
            """)
    QcOverviewVO selectOverview(@Param("deptIds") List<Long> deptIds);

    /**
     * 问题明细总条数与涉及病历数；deptIds 为科室数据权限收口集合（null = 全院），
     * 与质控单概览同口径（问题明细经 record_source + record_id 关联病历归属科室）
     */
    @Select("""
            <script>
            SELECT COUNT(*)                  AS issue_count,
                   COUNT(DISTINCT record_id) AS issue_record_count
            FROM biz_quality_control_issue
            WHERE 1 = 1
            <if test="deptIds != null"> AND (
                 EXISTS (SELECT 1 FROM biz_medical_record m WHERE m.del_flag = 0
                         AND biz_quality_control_issue.record_source = 'OUTPATIENT' AND m.id = biz_quality_control_issue.record_id
                         AND m.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>)
              OR EXISTS (SELECT 1 FROM biz_inpatient_record r WHERE r.del_flag = 0
                         AND biz_quality_control_issue.record_source = 'INPATIENT' AND r.id = biz_quality_control_issue.record_id
                         AND r.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>)
            )</if>
            </script>
            """)
    QcOverviewVO selectIssueTotals(@Param("deptIds") List<Long> deptIds);

    /**
     * 维度分布（问题条数 / 累计扣分）。维度中文由服务层用枚举补，不在这里 CASE WHEN ——
     * 中文只允许有一处定义。deptIds 为科室数据权限收口集合（null = 全院），与概览同口径
     */
    @Select("""
            <script>
            SELECT dimension                AS dimension,
                   COUNT(*)                 AS hit_count,
                   COALESCE(SUM(deduct), 0) AS deduct_total
            FROM biz_quality_control_issue
            WHERE 1 = 1
            <if test="deptIds != null"> AND (
                 EXISTS (SELECT 1 FROM biz_medical_record m WHERE m.del_flag = 0
                         AND biz_quality_control_issue.record_source = 'OUTPATIENT' AND m.id = biz_quality_control_issue.record_id
                         AND m.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>)
              OR EXISTS (SELECT 1 FROM biz_inpatient_record r WHERE r.del_flag = 0
                         AND biz_quality_control_issue.record_source = 'INPATIENT' AND r.id = biz_quality_control_issue.record_id
                         AND r.dept_id IN <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>)
            )</if>
            GROUP BY dimension
            </script>
            """)
    List<QcRuleMetricVO> selectDimensionStat(@Param("deptIds") List<Long> deptIds);

    /**
     * 规则命中统计。只有命中过的规则会出现在这里，未命中的由服务层用规则枚举补全 ——
     * 「0 命中」在界面上必须是观测值，不能因为 GROUP BY 而整条规则消失。
     */
    @Select("""
            SELECT rule_code                AS rule_code,
                   COUNT(*)                 AS hit_count,
                   COUNT(DISTINCT qc_id)    AS qc_count,
                   COALESCE(SUM(deduct), 0) AS deduct_total
            FROM biz_quality_control_issue
            GROUP BY rule_code
            """)
    List<QcRuleMetricVO> selectRuleStat();

    /**
     * 某质控单的问题明细。按严重度倒序、同级按规则编码 ——
     * 与引擎的排序口径一致，页面看到的第一条就是最该先改的那条。
     */
    @Select("""
            SELECT id, qc_id, qc_no, record_source, record_id, patient_id,
                   rule_code, rule_name, dimension, severity, deduct,
                   field_name, error_detail, suggestion, evidence, create_time
            FROM biz_quality_control_issue
            WHERE qc_id = #{qcId}
            ORDER BY severity DESC, rule_code ASC, id ASC
            """)
    List<QcIssueVO> listIssueByQc(@Param("qcId") Long qcId);

}
