package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.PacuQueryPageDTO;
import com.his.operation.entity.BizAnesthesiaPacu;
import com.his.operation.vo.PacuRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * PACU 复苏记录 Mapper。
 */
@Mapper
public interface BizAnesthesiaPacuMapper extends BaseMapper<BizAnesthesiaPacu> {

    String PROJECTION = """
            SELECT u.*,
                   a.admission_no,
                   r.record_status,
                   r.anesthesia_type,
                   r.anesthesia_end_time,
                   o.operation_room,
                   o.planned_operation_name,
                   o.actual_operation_name
            FROM biz_anesthesia_pacu u
                     LEFT JOIN biz_admission a ON a.admission_id = u.admission_id AND a.del_flag = 0
                     LEFT JOIN biz_anesthesia_record r ON r.id = u.record_id AND r.del_flag = 0
                     LEFT JOIN biz_operation_apply o ON o.id = u.apply_id AND o.del_flag = 0
            """;

    @Select(PROJECTION + """
            WHERE u.del_flag = 0
              AND (#{q.admissionId} IS NULL OR u.admission_id = #{q.admissionId})
              AND (#{q.applyId} IS NULL OR u.apply_id = #{q.applyId})
              AND (#{q.recordId} IS NULL OR u.record_id = #{q.recordId})
              AND (#{q.status} IS NULL OR u.status = #{q.status})
              AND (#{q.unchargedOnly} IS NULL OR #{q.unchargedOnly} = 0 OR u.charge_status <> 1)
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR u.pacu_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR u.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR u.record_no LIKE CONCAT('%', #{q.keyword}, '%'))
            ORDER BY FIELD(u.status, 0, 1), u.enter_time DESC, u.id DESC
            """)
    IPage<PacuRecordVO> selectPacuPage(IPage<PacuRecordVO> page, @Param("q") PacuQueryPageDTO query);

    @Select(PROJECTION + " WHERE u.del_flag = 0 AND u.id = #{pacuId}")
    PacuRecordVO selectVOById(@Param("pacuId") Long pacuId);

    @Select(PROJECTION + " WHERE u.del_flag = 0 AND u.record_id = #{recordId}")
    PacuRecordVO selectVOByRecord(@Param("recordId") Long recordId);

}
