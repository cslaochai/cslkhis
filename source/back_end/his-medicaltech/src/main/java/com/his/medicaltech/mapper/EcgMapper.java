package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.vo.EcgListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 心电工作台查询（裸 SQL，sql/173）。
 *
 * <p>与 sql/138 放射工作台同款口径：列表要按「是不是心电项目」过滤，而这条口径在
 * 检查项目字典的项目类型（3=心电图）上 —— 那是 his-system 的表，
 * 检查记录里只有 {@code inspection_item_code} 字符串外键，跨模块读一律裸 SQL 快照。
 */
@Mapper
public interface EcgMapper {

    /**
     * 工作台列表：检查记录 LEFT JOIN 波形 / Holter 分析 / 报告。
     *
     * <p>与放射台（只列 4/5/6）不同：心电的「签到 / 采集」也是本站的事，
     * 所以 1~6 全列，7-已取消排除。「待采集」栏就是 1~3。
     */
    @Select("""
            <script>
            SELECT rec.id AS record_id, rec.record_no, rec.apply_id, rec.apply_no,
                   rec.patient_id, rec.patient_no, rec.patient_name, rec.gender, rec.age,
                   rec.inspection_item_code AS item_code, rec.inspection_item_name AS item_name,
                   rec.body_part, rec.record_status,
                   rec.apply_dept_name, rec.apply_doctor_name, rec.clinical_diagnosis,
                   w.id AS wave_id, w.ecg_type, w.collect_by, w.collect_time,
                   m.id AS measure_id,
                   h.id AS holter_id, h.wear_start_time, h.wear_end_time, h.total_beats, h.avg_hr,
                   h.max_hr, h.min_hr, h.afib_flag, h.pvc_count, h.svc_count, h.longest_pause_ms,
                   r.id AS report_id, r.report_no, r.report_status,
                   r.positive_flag, r.is_critical, r.write_by, r.write_time,
                   r.audit_by, r.audit_time, r.publish_by, r.publish_time,
                   r.report_version, r.reject_reason,
                   rec.create_time
              FROM biz_inspection_record rec
              JOIN sys_inspection_item i
                ON i.item_code = rec.inspection_item_code AND i.del_flag = 0 AND i.item_type = 3
              LEFT JOIN biz_ecg_waveform w
                ON w.record_id = rec.id AND w.del_flag = 0
              LEFT JOIN biz_ecg_measure m
                ON m.record_id = rec.id AND m.del_flag = 0
              LEFT JOIN biz_ecg_holter h
                ON h.record_id = rec.id AND h.del_flag = 0
              LEFT JOIN biz_report r
                ON r.record_id = rec.id AND r.del_flag = 0 AND r.report_type = 1
             WHERE rec.del_flag = 0
               AND rec.record_status NOT IN (7)
               <if test="keyword != null and keyword != ''">
                 AND (rec.patient_name LIKE CONCAT('%', #{keyword}, '%')
                   OR rec.patient_no   LIKE CONCAT('%', #{keyword}, '%')
                   OR rec.record_no    LIKE CONCAT('%', #{keyword}, '%')
                   OR rec.inspection_item_name LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="collectPending != null and collectPending">
                 AND rec.record_status IN (1, 2, 3)
               </if>
               <if test="onlyUnwritten != null and onlyUnwritten">
                 AND r.id IS NULL AND rec.record_status NOT IN (1, 2, 3)
               </if>
               <if test="reportStatus != null"> AND r.report_status = #{reportStatus}</if>
               <if test="startDate != null and startDate != ''"> AND rec.create_time &gt;= #{startDate}</if>
               <if test="endDate != null and endDate != ''"> AND rec.create_time &lt;= CONCAT(#{endDate}, ' 23:59:59')</if>
             ORDER BY rec.create_time DESC, rec.id DESC
            </script>
            """)
    List<EcgListVO> selectWorkbenchPage(IPage<EcgListVO> page,
                                        @Param("keyword") String keyword,
                                        @Param("collectPending") Boolean collectPending,
                                        @Param("onlyUnwritten") Boolean onlyUnwritten,
                                        @Param("reportStatus") Integer reportStatus,
                                        @Param("startDate") String startDate,
                                        @Param("endDate") String endDate);

    /**
     * 按检查记录 ID 取工作台行（详情/写报告前定位用）。
     */
    @Select("""
            SELECT rec.id AS record_id, rec.record_no, rec.apply_id, rec.apply_no,
                   rec.patient_id, rec.patient_no, rec.patient_name, rec.gender, rec.age,
                   rec.inspection_item_code AS item_code, rec.inspection_item_name AS item_name,
                   rec.body_part, rec.record_status,
                   rec.apply_dept_name, rec.apply_doctor_name, rec.clinical_diagnosis,
                   w.id AS wave_id, w.ecg_type, w.collect_by, w.collect_time,
                   m.id AS measure_id,
                   h.id AS holter_id, h.wear_start_time, h.wear_end_time, h.total_beats, h.avg_hr,
                   h.max_hr, h.min_hr, h.afib_flag, h.pvc_count, h.svc_count, h.longest_pause_ms,
                   r.id AS report_id, r.report_no, r.report_status,
                   r.positive_flag, r.is_critical, r.write_by, r.write_time,
                   r.audit_by, r.audit_time, r.publish_by, r.publish_time,
                   r.report_version, r.reject_reason,
                   rec.create_time
              FROM biz_inspection_record rec
              JOIN sys_inspection_item i
                ON i.item_code = rec.inspection_item_code AND i.del_flag = 0 AND i.item_type = 3
              LEFT JOIN biz_ecg_waveform w
                ON w.record_id = rec.id AND w.del_flag = 0
              LEFT JOIN biz_ecg_measure m
                ON m.record_id = rec.id AND m.del_flag = 0
              LEFT JOIN biz_ecg_holter h
                ON h.record_id = rec.id AND h.del_flag = 0
              LEFT JOIN biz_report r
                ON r.record_id = rec.id AND r.del_flag = 0 AND r.report_type = 1
             WHERE rec.del_flag = 0 AND rec.id = #{recordId}
            """)
    EcgListVO selectWorkbenchByRecordId(@Param("recordId") Long recordId);
}
