package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.ConsultationQueryPageDTO;
import com.his.patient.entity.BizConsultation;
import com.his.patient.vo.ConsultationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 住院会诊 Mapper。
 *
 * <p>自定义 {@code @Select} 不受 {@code @TableLogic} 影响 → JOIN 里必须显式写 {@code del_flag = 0}。
 *
 * <p>两处刻意的做法：
 * <ol>
 *   <li><b>科室名用 JOIN 取，不写快照列</b>：申请/会诊科室ID 都是真实科室主键，
 *       JOIN 不到就显示空 —— 存量 6 行演示数据的科室ID 在科室里不存在，
 *       宁可显示空，也不替它们猜一个科室名。</li>
 *   <li><b>未完成排在前面用 FIELD() 显式指定顺序</b>：状态码是 0-待应答 / 1-已完成 / 2-已取消 /
 *       3-已应答，按码值升序会把"已应答（会诊中）"排到最后 —— 那正是最该被看见的那批。
 *       顺序必须是「待应答 → 已应答 → 已完成 → 已取消」。</li>
 * </ol>
 */
@Mapper
public interface BizConsultationMapper extends BaseMapper<BizConsultation> {

    /**
     * 会诊列表公共投影（列表 / 详情共用，避免两套口径）
     */
    String PROJECTION = """
            SELECT c.*,
                   p.patient_name,
                   p.patient_no,
                   a.admission_no,
                   b.bed_no,
                   fd.dept_name AS from_dept_name,
                   td.dept_name AS to_dept_name,
                   r.record_no
            FROM biz_consultation c
                     LEFT JOIN biz_patient p ON p.id = c.patient_id AND p.del_flag = 0
                     LEFT JOIN biz_admission a ON a.admission_id = c.admission_id AND a.del_flag = 0
                     LEFT JOIN sys_bed b ON b.bed_id = a.bed_id AND b.del_flag = 0
                     LEFT JOIN sys_department fd ON fd.id = c.from_dept_id AND fd.del_flag = 0
                     LEFT JOIN sys_department td ON td.id = c.to_dept_id AND td.del_flag = 0
                     LEFT JOIN biz_inpatient_record r ON r.id = c.record_id AND r.del_flag = 0
            """;

    /**
     * 会诊分页（申请方工作台 / 会诊科室工作台共用）
     */
    @Select(PROJECTION + """
            WHERE c.del_flag = 0
              AND (#{q.admissionId} IS NULL OR c.admission_id = #{q.admissionId})
              AND (#{q.patientId} IS NULL OR c.patient_id = #{q.patientId})
              AND (#{q.fromDeptId} IS NULL OR c.from_dept_id = #{q.fromDeptId})
              AND (#{q.toDeptId} IS NULL OR c.to_dept_id = #{q.toDeptId})
              AND (#{q.consultStatus} IS NULL OR c.consult_status = #{q.consultStatus})
              AND (#{q.consultType} IS NULL OR c.consult_type = #{q.consultType})
              AND (#{q.consultCategory} IS NULL OR c.consult_category = #{q.consultCategory})
              AND (#{q.isUrgent} IS NULL OR c.is_urgent = #{q.isUrgent})
              AND (#{q.unfinishedOnly} IS NULL OR #{q.unfinishedOnly} = 0 OR c.consult_status IN (0, 3))
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR c.consultation_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.reason LIKE CONCAT('%', #{q.keyword}, '%')
                   OR p.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR p.patient_no LIKE CONCAT('%', #{q.keyword}, '%'))
            ORDER BY FIELD(c.consult_status, 0, 3, 1, 2), c.is_urgent DESC, c.apply_time DESC, c.consultation_id DESC
            """)
    IPage<ConsultationVO> selectConsultationPage(IPage<ConsultationVO> page,
                                                 @Param("q") ConsultationQueryPageDTO query);

    /**
     * 会诊详情（含患者/床位/科室/回写病历号）
     */
    @Select(PROJECTION + " WHERE c.del_flag = 0 AND c.consultation_id = #{consultationId}")
    ConsultationVO selectConsultationById(@Param("consultationId") Long consultationId);

    /**
     * 未完成会诊数（待应答 + 已应答）：工作台角标用
     */
    @Select("""
            SELECT COUNT(*) FROM biz_consultation
            WHERE del_flag = 0
              AND consult_status IN (0, 3)
              AND (#{toDeptId} IS NULL OR to_dept_id = #{toDeptId})
              AND (#{admissionId} IS NULL OR admission_id = #{admissionId})
            """)
    long countUnfinished(@Param("toDeptId") Long toDeptId, @Param("admissionId") Long admissionId);

    /**
     * 当天已生成的会诊号条数（会诊号序号用）
     */
    @Select("SELECT COUNT(*) FROM biz_consultation WHERE del_flag = 0 AND consultation_no LIKE CONCAT(#{prefix}, '%')")
    long countByNoPrefix(@Param("prefix") String prefix);

    /**
     * 科室名（取不到返回 null，由调用方决定怎么显示 —— 绝不编一个科室名）
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);
}
