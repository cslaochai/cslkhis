package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.AnesthesiaFollowupQueryPageDTO;
import com.his.operation.entity.BizAnesthesiaFollowup;
import com.his.operation.vo.AnesthesiaFollowupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 麻醉术后随访 Mapper。
 *
 * <p>自定义 {@code @Select} 不受 {@code @TableLogic} 影响 → 必须显式写 {@code del_flag = 0}。
 */
@Mapper
public interface BizAnesthesiaFollowupMapper extends BaseMapper<BizAnesthesiaFollowup> {

    String PROJECTION = """
            SELECT f.*, p.patient_no
            FROM biz_anesthesia_followup f
                     LEFT JOIN biz_patient p ON p.id = f.patient_id AND p.del_flag = 0
            """;

    /** 随访分页（麻醉随访工作台） */
    @Select(PROJECTION + """
             WHERE f.del_flag = 0
               AND (#{q.recordId} IS NULL OR f.record_id = #{q.recordId})
               AND (#{q.admissionId} IS NULL OR f.admission_id = #{q.admissionId})
               AND (#{q.patientId} IS NULL OR f.patient_id = #{q.patientId})
               AND (#{q.followupStatus} IS NULL OR f.followup_status = #{q.followupStatus})
               AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                    OR f.followup_no LIKE CONCAT('%', #{q.keyword}, '%')
                    OR f.record_no LIKE CONCAT('%', #{q.keyword}, '%')
                    OR f.patient_name LIKE CONCAT('%', #{q.keyword}, '%'))
             ORDER BY f.followup_time DESC, f.id DESC
            """)
    IPage<AnesthesiaFollowupVO> selectFollowupPage(IPage<AnesthesiaFollowupVO> page,
                                                   @Param("q") AnesthesiaFollowupQueryPageDTO query);

    /** 随访详情 */
    @Select(PROJECTION + " WHERE f.del_flag = 0 AND f.id = #{id}")
    AnesthesiaFollowupVO selectFollowupById(@Param("id") Long id);

    /** 某条麻醉记录的全部随访（按轮次升序 = 这条链的发生顺序） */
    @Select(PROJECTION + """
             WHERE f.del_flag = 0 AND f.record_id = #{recordId}
             ORDER BY f.round_no ASC, f.id ASC
            """)
    List<AnesthesiaFollowupVO> selectByRecord(@Param("recordId") Long recordId);

    /**
     * 当天已生成的随访单号条数（单号序号用）。
     *
     * <p>⚠ 刻意<b>不过滤 del_flag</b>：{@code uk_followup_no} 不含 del_flag，
     * 草稿软删后行仍占号；若按 del_flag=0 计数，次日序号会撞回被删行的单号（Duplicate entry）。
     * 宁可跳号，不可撞号。
     */
    @Select("SELECT COUNT(*) FROM biz_anesthesia_followup WHERE followup_no LIKE CONCAT(#{prefix}, '%')")
    long countByNoPrefix(@Param("prefix") String prefix);

    /** 某条麻醉记录已有的最大轮次（新随访 = max+1；无行返回 0） */
    @Select("SELECT IFNULL(MAX(round_no), 0) FROM biz_anesthesia_followup WHERE del_flag = 0 AND record_id = #{recordId}")
    int maxRoundOf(@Param("recordId") Long recordId);

    /**
     * 随访欠账数（工作台角标）：麻醉记录已提交、结束已超过 24 小时，却还没有任何一条<b>已完成</b>的随访。
     *
     * <p>草稿不算还账 —— 随访的凭证是签了名的那份，不是"新建了一条没写完"。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_anesthesia_record r
            WHERE r.del_flag = 0
              AND r.record_status IN (1, 2)
              AND r.anesthesia_end_time IS NOT NULL
              AND r.anesthesia_end_time <= DATE_SUB(NOW(), INTERVAL 24 HOUR)
              AND NOT EXISTS (SELECT 1 FROM biz_anesthesia_followup f
                              WHERE f.del_flag = 0 AND f.record_id = r.id AND f.followup_status = 1)
            """)
    long countOverduePending();
}
