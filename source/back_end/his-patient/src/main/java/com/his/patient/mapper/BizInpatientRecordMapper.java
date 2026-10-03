package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.InpatientRecordQueryPageDTO;
import com.his.patient.entity.BizInpatientRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住院病历文书 Mapper。
 *
 * <p>分页查询**返回实体**而不是 VO：结构化率的计算口径在 Java 侧
 * （{@code RecordStructuredFields}），SQL 里算不出来。如果这里直接出 VO，
 * 就得在 SQL 里再实现一套要素清单 —— 那就成了两个口径。
 *
 * <p>自定义 {@code @Select} 不受 {@code @TableLogic} 影响 → 必须显式写 {@code del_flag = 0}。
 */
@Mapper
public interface BizInpatientRecordMapper extends BaseMapper<BizInpatientRecord> {

    /**
     * 病历文书分页（返回实体，结构化率由服务层按统一口径计算）
     * <p>{@code scopeDeptIds} 是科室数据权限（M6）的服务端收敛集合，前端不可见。
     */
    @Select("""
            <script>
            SELECT r.*
            FROM biz_inpatient_record r
            WHERE r.del_flag = 0
              AND (#{q.admissionId} IS NULL OR r.admission_id = #{q.admissionId})
              AND (#{q.patientId} IS NULL OR r.patient_id = #{q.patientId})
              AND (#{q.recordType} IS NULL OR r.record_type = #{q.recordType})
              AND (#{q.recordStatus} IS NULL OR r.record_status = #{q.recordStatus})
              <if test="q.scopeDeptIds != null and q.scopeDeptIds.size() > 0">
                AND r.dept_id IN
                <foreach collection="q.scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
              </if>
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR r.record_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.record_title LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.chief_complaint LIKE CONCAT('%', #{q.keyword}, '%')
                   OR r.diagnosis_name LIKE CONCAT('%', #{q.keyword}, '%'))
            ORDER BY r.record_time DESC, r.id DESC
            </script>
            """)
    IPage<BizInpatientRecord> selectRecordPage(IPage<BizInpatientRecord> page,
                                               @Param("q") InpatientRecordQueryPageDTO query);

    /**
     * 某次住院的全部文书（结构化率统计用，不分页）
     */
    @Select("""
            SELECT r.* FROM biz_inpatient_record r
            WHERE r.del_flag = 0 AND r.admission_id = #{admissionId}
            ORDER BY r.record_type ASC, r.record_time ASC, r.id ASC
            """)
    List<BizInpatientRecord> selectByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 某次住院的文书号集合（日志按住院查询时先取号，再按号过滤日志）
     */
    @Select("""
            SELECT r.record_no FROM biz_inpatient_record r
            WHERE r.del_flag = 0 AND r.admission_id = #{admissionId}
            """)
    List<String> selectRecordNosByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 当天已生成的文书号条数（文书号序号用）
     */
    @Select("SELECT COUNT(*) FROM biz_inpatient_record WHERE del_flag = 0 AND record_no LIKE CONCAT(#{prefix}, '%')")
    long countByRecordNoPrefix(@Param("prefix") String prefix);
}
