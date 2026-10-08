package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.AnesthesiaRecordQueryPageDTO;
import com.his.operation.entity.BizAnesthesiaRecord;
import com.his.operation.vo.AnesthesiaRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 麻醉记录单 Mapper。
 */
@Mapper
public interface BizAnesthesiaRecordMapper extends BaseMapper<BizAnesthesiaRecord> {

    String PROJECTION = """
            SELECT r.*,
                   p.patient_no,
                   a.admission_no,
                   v.visit_no,
                   v.visit_status,
                   v.conclusion                      AS visit_conclusion,
                   o.planned_operation_name,
                   o.actual_operation_name,
                   o.operation_room,
                   o.planned_start_time,
                   o.operation_status,
                   o.anesthesia_type                 AS apply_anesthesia_type,
                   o.surgeon_name,
                   o.is_emergency
            FROM biz_anesthesia_record r
                     LEFT JOIN biz_patient p ON p.id = r.patient_id AND p.del_flag = 0
                     LEFT JOIN biz_admission a ON a.admission_id = r.admission_id AND a.del_flag = 0
                     LEFT JOIN biz_anesthesia_visit v ON v.id = r.visit_id AND v.del_flag = 0
                     LEFT JOIN biz_operation_apply o ON o.id = r.apply_id AND o.del_flag = 0
            """;

    @Select(PROJECTION + """
            WHERE r.del_flag = 0
              AND (#{q.admissionId} IS NULL OR r.admission_id = #{q.admissionId})
              AND (#{q.applyId} IS NULL OR r.apply_id = #{q.applyId})
              AND (#{q.anesthetistId} IS NULL OR r.anesthetist_id = #{q.anesthetistId})
              AND (#{q.recordStatus} IS NULL OR r.record_status = #{q.recordStatus})
              AND (#{q.unchargedOnly} IS NULL OR #{q.unchargedOnly} = 0 OR r.charge_status <> 1)
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR r.record_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.apply_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR o.planned_operation_name LIKE CONCAT('%', #{q.keyword}, '%'))
            ORDER BY FIELD(r.record_status, 0, 1, 2), r.create_time DESC, r.id DESC
            """)
    IPage<AnesthesiaRecordVO> selectRecordPage(IPage<AnesthesiaRecordVO> page,
                                               @Param("q") AnesthesiaRecordQueryPageDTO query);

    @Select(PROJECTION + " WHERE r.del_flag = 0 AND r.id = #{recordId}")
    AnesthesiaRecordVO selectVOById(@Param("recordId") Long recordId);

    @Select(PROJECTION + " WHERE r.del_flag = 0 AND r.apply_id = #{applyId}")
    AnesthesiaRecordVO selectVOByApply(@Param("applyId") Long applyId);

}
