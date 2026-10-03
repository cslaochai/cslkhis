package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.AnesthesiaVisitQueryPageDTO;
import com.his.operation.entity.BizAnesthesiaVisit;
import com.his.operation.vo.AnesthesiaVisitVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 麻醉术前访视单 Mapper。
 *
 * <p>自定义 {@code @Select} 不受 {@code @TableLogic} 影响 → 必须显式写 {@code del_flag = 0}。
 */
@Mapper
public interface BizAnesthesiaVisitMapper extends BaseMapper<BizAnesthesiaVisit> {

    String PROJECTION = """
            SELECT v.*,
                   p.patient_no,
                   a.admission_no,
                   o.planned_operation_name,
                   o.planned_start_time,
                   o.operation_room,
                   o.surgeon_name,
                   o.operation_status,
                   o.is_emergency
            FROM biz_anesthesia_visit v
                     LEFT JOIN biz_patient p ON p.id = v.patient_id AND p.del_flag = 0
                     LEFT JOIN biz_admission a ON a.admission_id = v.admission_id AND a.del_flag = 0
                     LEFT JOIN biz_operation_apply o ON o.id = v.apply_id AND o.del_flag = 0
            """;

    @Select(PROJECTION + """
            WHERE v.del_flag = 0
              AND (#{q.admissionId} IS NULL OR v.admission_id = #{q.admissionId})
              AND (#{q.applyId} IS NULL OR v.apply_id = #{q.applyId})
              AND (#{q.conclusion} IS NULL OR v.conclusion = #{q.conclusion})
              AND (#{q.unfinishedOnly} IS NULL OR #{q.unfinishedOnly} = 0 OR v.conclusion IS NULL)
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR v.visit_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR v.apply_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR v.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR o.planned_operation_name LIKE CONCAT('%', #{q.keyword}, '%'))
            ORDER BY v.conclusion IS NULL DESC, v.visit_time DESC, v.id DESC
            """)
    IPage<AnesthesiaVisitVO> selectVisitPage(IPage<AnesthesiaVisitVO> page,
                                            @Param("q") AnesthesiaVisitQueryPageDTO query);

    @Select(PROJECTION + " WHERE v.del_flag = 0 AND v.id = #{visitId}")
    AnesthesiaVisitVO selectVOById(@Param("visitId") Long visitId);

    @Select(PROJECTION + " WHERE v.del_flag = 0 AND v.apply_id = #{applyId}")
    AnesthesiaVisitVO selectVOByApply(@Param("applyId") Long applyId);

    /** 当天已生成的访视单号条数（单号序号用） */
    @Select("SELECT COUNT(*) FROM biz_anesthesia_visit WHERE del_flag = 0 AND visit_no LIKE CONCAT(#{prefix}, '%')")
    long countByNoPrefix(@Param("prefix") String prefix);

    /**
     * 该手术申请是否已有访视单（UNIQUE apply_id 的第二道防线，用于给人类可读的错误文案）。
     */
    @Select("SELECT COUNT(*) FROM biz_anesthesia_visit WHERE del_flag = 0 AND apply_id = #{applyId}")
    long countByApply(@Param("applyId") Long applyId);

    /**
     * 「麻醉前未完成有效访视」的数量 —— 急诊超前麻醉后必须能一眼看出谁还没补。
     *
     * <p>只算**在册临床取值**：访视不存在，或结论不是"可施行麻醉"，都算没过闸门。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_operation_apply a
            WHERE a.del_flag = 0 AND a.operation_status = 3
              AND NOT EXISTS (SELECT 1 FROM biz_anesthesia_visit v
                               WHERE v.del_flag = 0 AND v.apply_id = a.id AND v.conclusion = 1)
            """)
    long countFinishedWithoutVisit();
}
