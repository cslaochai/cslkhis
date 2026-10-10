package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.PacuQueryPageDTO;
import com.his.operation.entity.BizAnesthesiaPacu;
import com.his.operation.vo.PacuRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    @Select("<script>\n" + PROJECTION + """
            WHERE u.del_flag = 0
              AND (#{q.admissionId} IS NULL OR u.admission_id = #{q.admissionId})
              AND (#{q.applyId} IS NULL OR u.apply_id = #{q.applyId})
              AND (#{q.recordId} IS NULL OR u.record_id = #{q.recordId})
              AND (#{q.status} IS NULL OR u.status = #{q.status})
              AND (#{q.unchargedOnly} IS NULL OR #{q.unchargedOnly} = 0 OR u.charge_status &lt;&gt; 1)
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR u.pacu_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR u.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR u.record_no LIKE CONCAT('%', #{q.keyword}, '%'))
              <if test="deptIds != null">
                AND u.apply_id IN (SELECT o2.id FROM biz_operation_apply o2
                                    WHERE o2.del_flag = 0 AND o2.apply_dept_id IN
                                    <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>)
              </if>
            ORDER BY FIELD(u.status, 0, 1), u.enter_time DESC, u.id DESC
            </script>
            """)
    IPage<PacuRecordVO> selectPacuPage(IPage<PacuRecordVO> page, @Param("q") PacuQueryPageDTO query,
                                       @Param("deptIds") List<Long> deptIds);

    @Select(PROJECTION + " WHERE u.del_flag = 0 AND u.id = #{pacuId}")
    PacuRecordVO selectVOById(@Param("pacuId") Long pacuId);

    @Select(PROJECTION + " WHERE u.del_flag = 0 AND u.record_id = #{recordId}")
    PacuRecordVO selectVOByRecord(@Param("recordId") Long recordId);

    /**
     * 在室观察数（工作台角标）；科室范围经手术申请单 apply_dept_id 收口
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_anesthesia_pacu u
            WHERE u.del_flag = 0 AND u.status = #{status}
              AND u.apply_id IN (SELECT o.id FROM biz_operation_apply o
                                  WHERE o.del_flag = 0 AND o.apply_dept_id IN
                                  <foreach collection="deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>)
            </script>
            """)
    long countInRoom(@Param("status") Integer status, @Param("deptIds") List<Long> deptIds);

}
