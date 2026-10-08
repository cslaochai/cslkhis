package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.vo.RadioReportListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 放射诊断工作台查询（裸 SQL，sql/138）。
 */
@Mapper
public interface RadioReportMapper {

    /**
     * 工作台列表：检查记录 LEFT JOIN 报告。
     *
     * <p>{@code record_status IN (4,5,6)} 是刻意的：1已登记 / 2已签到 / 3检查中都还没拍出片子，
     * 诊断医师在阅片器里没东西可看，让它们出现在诊断台只会造成「点进去一片空白」。
     */
    @Select("""
            <script>
            SELECT rec.id AS record_id, rec.record_no, rec.apply_id, rec.apply_no,
                   rec.patient_id, rec.patient_no, rec.patient_name, rec.gender, rec.age,
                   rec.inspection_item_code AS item_code, rec.inspection_item_name AS item_name,
                   rec.body_part, rec.record_status,
                   rec.apply_dept_name, rec.apply_doctor_name, rec.clinical_diagnosis,
                   rec.report_sign_id, rec.audit_sign_id,
                   r.id AS report_id, r.report_no, r.report_status, r.exam_method,
                   r.positive_flag, r.is_critical, r.write_by, r.write_time,
                   r.audit_by, r.audit_time, r.publish_by, r.publish_time,
                   r.film_count, r.report_version, r.reject_reason,
                   rec.create_time
              FROM biz_inspection_record rec
              JOIN sys_inspection_item i
                ON i.item_code = rec.inspection_item_code AND i.del_flag = 0 AND i.item_type = 1
              LEFT JOIN biz_report r
                ON r.record_id = rec.id AND r.del_flag = 0 AND r.report_type = ${@com.his.medicaltech.enums.ReportTypeEnum@INSPECTION.getCode()}
             WHERE rec.del_flag = 0
               AND rec.record_status IN (4, 5, 6)
               <if test="keyword != null and keyword != ''">
                 AND (rec.patient_name LIKE CONCAT('%', #{keyword}, '%')
                   OR rec.patient_no   LIKE CONCAT('%', #{keyword}, '%')
                   OR rec.record_no    LIKE CONCAT('%', #{keyword}, '%')
                   OR rec.inspection_item_name LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="reportStatus != null"> AND r.report_status = #{reportStatus}</if>
               <if test="positiveFlag != null"> AND r.positive_flag = #{positiveFlag}</if>
               <if test="onlyUnwritten != null and onlyUnwritten"> AND r.id IS NULL</if>
               <if test="startDate != null and startDate != ''"> AND rec.create_time &gt;= #{startDate}</if>
               <if test="endDate != null and endDate != ''"> AND rec.create_time &lt;= CONCAT(#{endDate}, ' 23:59:59')</if>
             ORDER BY rec.create_time DESC, rec.id DESC
            </script>
            """)
    java.util.List<RadioReportListVO> selectWorkbenchPage(IPage<RadioReportListVO> page,
                                                          @Param("keyword") String keyword,
                                                          @Param("reportStatus") Integer reportStatus,
                                                          @Param("positiveFlag") Integer positiveFlag,
                                                          @Param("onlyUnwritten") Boolean onlyUnwritten,
                                                          @Param("startDate") String startDate,
                                                          @Param("endDate") String endDate);

    /**
     * 查项目是不是放射（检查项目字典的项目类型，1=放射）。
     *
     * <p>跨模块读 his-system 的表，所以裸 SQL 不建实体：这张表在医技侧只有
     * 「项目类型」这一个字段有意义，为它建实体等于把整张字典表搬进本模块。
     *
     * @return 1-放射 2-超声 3-心电图 4-内镜 5-其他；查不到返回 null
     */
    @Select("""
            SELECT item_type
              FROM sys_inspection_item
             WHERE del_flag = 0 AND item_code = #{itemCode}
             LIMIT 1
            """)
    Integer selectItemTypeByCode(@Param("itemCode") String itemCode);

    /**
     * 按检查记录 ID 取工作台行（详情/写报告前定位用）。
     */
    @Select("""
            SELECT rec.id AS record_id, rec.record_no, rec.apply_id, rec.apply_no,
                   rec.patient_id, rec.patient_no, rec.patient_name, rec.gender, rec.age,
                   rec.inspection_item_code AS item_code, rec.inspection_item_name AS item_name,
                   rec.body_part, rec.record_status,
                   rec.apply_dept_name, rec.apply_doctor_name, rec.clinical_diagnosis,
                   rec.report_sign_id, rec.audit_sign_id,
                   r.id AS report_id, r.report_no, r.report_status, r.exam_method,
                   r.positive_flag, r.is_critical, r.write_by, r.write_time,
                   r.audit_by, r.audit_time, r.publish_by, r.publish_time,
                   r.film_count, r.report_version, r.reject_reason,
                   rec.create_time
              FROM biz_inspection_record rec
              JOIN sys_inspection_item i
                ON i.item_code = rec.inspection_item_code AND i.del_flag = 0 AND i.item_type = 1
              LEFT JOIN biz_report r
                ON r.record_id = rec.id AND r.del_flag = 0 AND r.report_type = ${@com.his.medicaltech.enums.ReportTypeEnum@INSPECTION.getCode()}
             WHERE rec.del_flag = 0 AND rec.id = #{recordId}
            """)
    RadioReportListVO selectWorkbenchByRecordId(@Param("recordId") Long recordId);

    /**
     * 本次检查已登记的胶片张数（写报告时展示「已打几张片」；统计口径以检查胶片用量为准）。
     *
     * <p>只数未作废的：作废的胶片既没发给患者也没收钱，算进张数会让报告上的数字对不上实物。
     *
     * <p><b>为什么写 {@code NOT IN (4)} 而不是 {@code <> 4}</b>：注解里的 {@code <script>} 走 XML 解析，
     * 尖括号会被当标签吞掉一半（实测 {@code &lt;>} 到了 MySQL 侧只剩 {@code >}，报
     * "syntax error near '> 4'"，且这种错误在启动时零征兆、要跑到这一行才炸）。
     * 写 {@code !=} / {@code NOT IN} 可以彻底绕开尖括号。
     */
    @Select("""
            SELECT COALESCE(SUM(quantity), 0)
              FROM biz_exam_film
             WHERE del_flag = 0 AND record_id = #{recordId} AND film_status NOT IN (4)
            """)
    Integer sumFilmQuantity(@Param("recordId") Long recordId);
}
