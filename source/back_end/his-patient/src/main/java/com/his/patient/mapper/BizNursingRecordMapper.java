package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.NursingRecordQueryPageDTO;
import com.his.patient.entity.BizNursingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 护理文书 Mapper。
 *
 * <p>自定义 {@code @Select} 不受 {@code @TableLogic} 影响 → 必须显式写 {@code del_flag = 0}。
 */
@Mapper
public interface BizNursingRecordMapper extends BaseMapper<BizNursingRecord> {

    /**
     * 护理文书分页（返回实体，展示态由服务层算：measureDate/measureClock/血压文案等）
     * <p>{@code scopeDeptIds} 是科室数据权限（M6）的服务端收敛集合，前端不可见。
     */
    @Select("""
            <script>
            SELECT n.*
            FROM biz_nursing_record n
            WHERE n.del_flag = 0
              AND (#{q.admissionId} IS NULL OR n.admission_id = #{q.admissionId})
              AND (#{q.patientId} IS NULL OR n.patient_id = #{q.patientId})
              AND (#{q.nursingType} IS NULL OR n.nursing_type = #{q.nursingType})
              AND (#{q.recordStatus} IS NULL OR n.record_status = #{q.recordStatus})
              <if test="q.scopeDeptIds != null and q.scopeDeptIds.size() > 0">
                AND n.dept_id IN
                <foreach collection="q.scopeDeptIds" item="sd" open="(" separator="," close=")">#{sd}</foreach>
              </if>
              AND (#{q.beginTime} IS NULL OR #{q.beginTime} = '' OR n.measure_time &gt;= #{q.beginTime})
              AND (#{q.endTime} IS NULL OR #{q.endTime} = '' OR n.measure_time &lt;= #{q.endTime})
            ORDER BY n.measure_time ASC, n.id ASC
            </script>
            """)
    IPage<BizNursingRecord> selectNursingPage(IPage<BizNursingRecord> page,
                                              @Param("q") NursingRecordQueryPageDTO query);

    /**
     * 某时间段的记录（三测单用：按测量时间升序，一次取全，不走分页 —— 曲线不能断页）
     */
    @Select("""
            SELECT n.* FROM biz_nursing_record n
            WHERE n.del_flag = 0
              AND n.admission_id = #{admissionId}
              AND n.nursing_type = #{nursingType}
              AND (#{beginTime} IS NULL OR n.measure_time >= #{beginTime})
              AND (#{endTime} IS NULL OR n.measure_time <= #{endTime})
            ORDER BY n.measure_time ASC, n.id ASC
            """)
    List<BizNursingRecord> selectByAdmissionAndType(@Param("admissionId") Long admissionId,
                                                    @Param("nursingType") Integer nursingType,
                                                    @Param("beginTime") String beginTime,
                                                    @Param("endTime") String endTime);

    /**
     * 某时间段的记录（多类型版，出入量小结用：三测单与生命体征监测都带出入量字段）
     */
    @Select("""
            <script>
            SELECT n.* FROM biz_nursing_record n
            WHERE n.del_flag = 0
              AND n.admission_id = #{admissionId}
              AND n.nursing_type IN
              <foreach collection="nursingTypes" item="t" open="(" separator="," close=")">#{t}</foreach>
              AND (#{beginTime} IS NULL OR #{beginTime} = '' OR n.measure_time &gt;= #{beginTime})
              AND (#{endTime} IS NULL OR #{endTime} = '' OR n.measure_time &lt;= #{endTime})
            ORDER BY n.measure_time ASC, n.id ASC
            </script>
            """)
    List<BizNursingRecord> selectByAdmissionAndTypes(@Param("admissionId") Long admissionId,
                                                     @Param("nursingTypes") java.util.List<Integer> nursingTypes,
                                                     @Param("beginTime") String beginTime,
                                                     @Param("endTime") String endTime);

    /**
     * 某次住院的护理文书号集合（日志按住院查询时先取号）
     */
    @Select("""
            SELECT n.record_no FROM biz_nursing_record n
            WHERE n.del_flag = 0 AND n.admission_id = #{admissionId}
            """)
    List<String> selectRecordNosByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 当天已生成的文书号条数（文书号序号用）
     */
    @Select("SELECT COUNT(*) FROM biz_nursing_record WHERE del_flag = 0 AND record_no LIKE CONCAT(#{prefix}, '%')")
    long countByRecordNoPrefix(@Param("prefix") String prefix);
}
