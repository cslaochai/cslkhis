package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.patient.dto.VteEventQueryPageDTO;
import com.his.patient.entity.BizVteEvent;
import com.his.patient.vo.VteEventVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * VTE 事件 Mapper（排序：确诊日期倒序，最新发生的事在最上面）
 */
@Mapper
public interface BizVteEventMapper extends BaseMapper<BizVteEvent> {

    /**
     * 单号前缀当日已用最大序号（VE+yyyyMMdd+4位）
     */
    @Select("SELECT COALESCE(MAX(CAST(RIGHT(event_no, 4) AS UNSIGNED)), 0) "
            + "FROM biz_vte_event WHERE event_no LIKE CONCAT(#{prefix}, '%')")
    long maxEventSeq(@Param("prefix") String prefix);

    @Select("""
            <script>
            SELECT e.*,
                   CASE e.event_type WHEN 1 THEN '深静脉血栓（DVT）' WHEN 2 THEN '肺栓塞（PE）'
                                     WHEN 3 THEN '预防相关出血' ELSE '未知' END AS event_type_text,
                   CASE e.onset_type WHEN 1 THEN '院内发生' WHEN 2 THEN '入院时已存在' ELSE '未知' END AS onset_type_text,
                   CASE e.diagnosis_basis WHEN 1 THEN '超声' WHEN 2 THEN 'CT 肺动脉造影' WHEN 3 THEN '静脉造影'
                                          WHEN 4 THEN '临床诊断' WHEN 5 THEN '其他' ELSE '未填' END AS diagnosis_basis_text,
                   CASE e.outcome WHEN 1 THEN '好转' WHEN 2 THEN '未愈' WHEN 3 THEN '死亡'
                                  WHEN 4 THEN '未知' ELSE '未填' END AS outcome_text,
                   CASE WHEN e.event_type IN (1, 2) AND e.onset_type = 1 THEN 1 ELSE 0 END AS counted
              FROM biz_vte_event e
             WHERE e.del_flag = 0
               AND (#{q.admissionId} IS NULL OR e.admission_id = #{q.admissionId})
               AND (#{q.eventType} IS NULL OR e.event_type = #{q.eventType})
               AND (#{q.onsetType} IS NULL OR e.onset_type = #{q.onsetType})
               AND (#{q.beginDate} IS NULL OR e.diagnose_date &gt;= #{q.beginDate})
               AND (#{q.endDate} IS NULL OR e.diagnose_date &lt;= #{q.endDate})
               AND (#{q.keyword} IS NULL OR #{q.keyword} = '' OR e.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                    OR e.patient_no LIKE CONCAT('%', #{q.keyword}, '%'))
             ORDER BY e.diagnose_date DESC, e.id DESC
            </script>
            """)
    IPage<VteEventVO> selectEventPage(Page<VteEventVO> page, @Param("q") VteEventQueryPageDTO query);

    @Select("""
            SELECT e.*,
                   CASE e.event_type WHEN 1 THEN '深静脉血栓（DVT）' WHEN 2 THEN '肺栓塞（PE）'
                                     WHEN 3 THEN '预防相关出血' ELSE '未知' END AS event_type_text,
                   CASE e.onset_type WHEN 1 THEN '院内发生' WHEN 2 THEN '入院时已存在' ELSE '未知' END AS onset_type_text,
                   CASE e.diagnosis_basis WHEN 1 THEN '超声' WHEN 2 THEN 'CT 肺动脉造影' WHEN 3 THEN '静脉造影'
                                          WHEN 4 THEN '临床诊断' WHEN 5 THEN '其他' ELSE '未填' END AS diagnosis_basis_text,
                   CASE e.outcome WHEN 1 THEN '好转' WHEN 2 THEN '未愈' WHEN 3 THEN '死亡'
                                  WHEN 4 THEN '未知' ELSE '未填' END AS outcome_text,
                   CASE WHEN e.event_type IN (1, 2) AND e.onset_type = 1 THEN 1 ELSE 0 END AS counted
              FROM biz_vte_event e
             WHERE e.del_flag = 0 AND e.admission_id = #{admissionId}
             ORDER BY e.diagnose_date DESC, e.id DESC
            """)
    List<VteEventVO> selectByAdmission(@Param("admissionId") Long admissionId);
}
