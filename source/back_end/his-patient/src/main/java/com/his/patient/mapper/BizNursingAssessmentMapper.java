package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.patient.dto.NursingAssessmentQueryPageDTO;
import com.his.patient.entity.BizNursingAssessment;
import com.his.patient.vo.NursingAssessmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 护理评估单 Mapper。
 */
@Mapper
public interface BizNursingAssessmentMapper extends BaseMapper<BizNursingAssessment> {

    /**
     * 分页查询（快照列冗余在表上，无需 JOIN）
     */
    @Select("""
            <script>
            SELECT a.*,
                   CASE a.assess_type WHEN 1 THEN '压疮评估（Braden）' WHEN 2 THEN '跌倒评估（Morse）'
                                      WHEN 3 THEN '疼痛评估（NRS）' WHEN 4 THEN 'VTE血栓评估（Caprini）'
                                      WHEN 5 THEN '管路滑脱评估' ELSE '未知(0)' END AS assess_type_text,
                   CASE a.risk_level WHEN 1 THEN '低风险' WHEN 2 THEN '中风险' WHEN 3 THEN '高风险'
                                     WHEN 4 THEN '极高风险' ELSE '未知(0)' END AS risk_level_text
              FROM biz_nursing_assessment a
             WHERE a.del_flag = 0
               AND (#{q.admissionId} IS NULL OR a.admission_id = #{q.admissionId})
               AND (#{q.assessType} IS NULL OR a.assess_type = #{q.assessType})
               AND (#{q.wardId} IS NULL OR a.ward_id = #{q.wardId})
               AND (#{q.patientName} IS NULL OR #{q.patientName} = '' OR a.patient_name LIKE CONCAT('%', #{q.patientName}, '%'))
             ORDER BY a.assess_time DESC, a.id DESC
            </script>
            """)
    IPage<NursingAssessmentVO> selectAssessPage(Page<NursingAssessmentVO> page,
                                                @Param("q") NursingAssessmentQueryPageDTO query);

    /**
     * 专项透视：该入院记录下**每类量表最新一条**（五类 1~5 各取 assess_time 最大行）。
     * ROW_NUMBER 同秒并列按 id DESC 稳定（与分页排序口径一致）；没评过的类型不返回。
     */
    @Select("""
            SELECT * FROM (
              SELECT a.*,
                     CASE a.assess_type WHEN 1 THEN '压疮评估（Braden）' WHEN 2 THEN '跌倒评估（Morse）'
                                        WHEN 3 THEN '疼痛评估（NRS）' WHEN 4 THEN 'VTE血栓评估（Caprini）'
                                        WHEN 5 THEN '管路滑脱评估' ELSE '未知(0)' END AS assess_type_text,
                     CASE a.risk_level WHEN 1 THEN '低风险' WHEN 2 THEN '中风险' WHEN 3 THEN '高风险'
                                       WHEN 4 THEN '极高风险' ELSE '未知(0)' END AS risk_level_text,
                     ROW_NUMBER() OVER (PARTITION BY a.assess_type ORDER BY a.assess_time DESC, a.id DESC) AS rn
                FROM biz_nursing_assessment a
               WHERE a.del_flag = 0
                 AND a.admission_id = #{admissionId}
                 AND a.assess_type IN (1, 2, 3, 4, 5)
            ) t
             WHERE t.rn = 1
             ORDER BY t.assess_type
            """)
    List<NursingAssessmentVO> selectLatestByAdmission(@Param("admissionId") Long admissionId);
}
