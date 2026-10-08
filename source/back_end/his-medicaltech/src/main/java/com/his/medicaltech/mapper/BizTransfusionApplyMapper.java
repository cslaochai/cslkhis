package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.dto.TransfusionApplyQueryPageDTO;
import com.his.medicaltech.entity.BizTransfusionApply;
import com.his.medicaltech.vo.TransfusionApplyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住院输血申请单 Mapper。
 */
@Mapper
public interface BizTransfusionApplyMapper extends BaseMapper<BizTransfusionApply> {

    /**
     * 列表/详情公共投影（见类注释：不 JOIN 任何主表已有快照的列）
     */
    String PROJECTION = """
            SELECT c.*,
                   a.admit_status,
                   r.record_no,
                   (SELECT COUNT(*) FROM biz_transfusion_bag b
                     WHERE b.del_flag = 0 AND b.apply_id = c.id) AS matched_bag_count
            FROM biz_transfusion_apply c
                     LEFT JOIN biz_admission a ON a.admission_id = c.admission_id AND a.del_flag = 0
                     LEFT JOIN biz_inpatient_record r ON r.id = c.record_id AND r.del_flag = 0
            """;

    /**
     * 输血申请分页（输血科配血工作台 / 病区申请方工作台共用）
     */
    @Select(PROJECTION + """
            WHERE c.del_flag = 0
              AND (#{q.admissionId} IS NULL OR c.admission_id = #{q.admissionId})
              AND (#{q.patientId} IS NULL OR c.patient_id = #{q.patientId})
              AND (#{q.applyDeptId} IS NULL OR c.apply_dept_id = #{q.applyDeptId})
              AND (#{q.transfusionStatus} IS NULL OR c.transfusion_status = #{q.transfusionStatus})
              AND (#{q.crossmatchStatus} IS NULL OR c.crossmatch_status = #{q.crossmatchStatus})
              AND (#{q.approveStatus} IS NULL OR c.approve_status = #{q.approveStatus})
              AND (#{q.bloodComponent} IS NULL OR c.blood_component = #{q.bloodComponent})
              AND (#{q.patientAbo} IS NULL OR #{q.patientAbo} = '' OR c.patient_abo = #{q.patientAbo})
              AND (#{q.hasReaction} IS NULL OR c.has_reaction = #{q.hasReaction})
              AND (#{q.unfinishedOnly} IS NULL OR #{q.unfinishedOnly} = 0
                   OR c.transfusion_status IN (0, 1, 2, 3))
              AND (#{q.applyDateFrom} IS NULL OR c.apply_time >= #{q.applyDateFrom})
              AND (#{q.applyDateTo} IS NULL OR c.apply_time < #{q.applyDateTo})
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR c.apply_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.admission_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.patient_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.transfusion_purpose LIKE CONCAT('%', #{q.keyword}, '%')
                   OR c.indication LIKE CONCAT('%', #{q.keyword}, '%'))
            ORDER BY FIELD(c.transfusion_status, 0, 1, 2, 3, 4, 5), c.apply_time DESC, c.id DESC
            """)
    IPage<TransfusionApplyVO> selectApplyPage(IPage<TransfusionApplyVO> page,
                                              @Param("q") TransfusionApplyQueryPageDTO query);

    /**
     * 输血申请详情
     */
    @Select(PROJECTION + " WHERE c.del_flag = 0 AND c.id = #{applyId}")
    TransfusionApplyVO selectApplyById(@Param("applyId") Long applyId);

    /**
     * 某次住院的全部输血申请（按申请时间升序 = 这条链的发生顺序）
     */
    @Select(PROJECTION + """
             WHERE c.del_flag = 0 AND c.admission_id = #{admissionId}
             ORDER BY c.apply_time ASC, c.id ASC
            """)
    List<TransfusionApplyVO> selectByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 当天已生成的输血单号条数（单号序号用）
     */
    @Select("SELECT COUNT(*) FROM biz_transfusion_apply WHERE del_flag = 0 AND apply_no LIKE CONCAT(#{prefix}, '%')")
    long countByNoPrefix(@Param("prefix") String prefix);

    /**
     * 未完成输血数（待配血 + 已配血 + 已发血 + 输注中）：工作台角标用
     */
    @Select("""
            SELECT COUNT(*) FROM biz_transfusion_apply
            WHERE del_flag = 0 AND transfusion_status IN (0, 1, 2, 3)
              AND (#{admissionId} IS NULL OR admission_id = #{admissionId})
            """)
    long countUnfinished(@Param("admissionId") Long admissionId);

    /**
     * 同一次住院是否已有"在途/未完成"的同一品种申请（防重复发起）。
     *
     * <p>口径与手术闭环"同一术式不允许并存两条未完成申请"一致：
     * 同一袋血申请两次，四核对里就是"重复"。
     *
     * <p><b>注意"已完成（4）"不在此列</b>：一个患者多批次输同一种血是常规操作
     * （今天 2U、明天再 2U），把已完成也算重复会拦住正常医疗。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_transfusion_apply
            WHERE del_flag = 0
              AND admission_id = #{admissionId}
              AND transfusion_status IN (0, 1, 2, 3)
              AND blood_component = #{component}
              AND (#{excludeId} IS NULL OR id <> #{excludeId})
            """)
    long countUnfinishedSameComponent(@Param("admissionId") Long admissionId,
                                      @Param("component") Integer component,
                                      @Param("excludeId") Long excludeId);

    /**
     * 科室名（取不到返回 null，由调用方渲染「未知科室(ID=x)」，绝不编一个名字）
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    /**
     * 员工姓名（申请/配血/发血/核对/输注/上报一律服务端查名，不信任前端传来的姓名）
     */
    @Select("SELECT emp_name FROM sys_employee WHERE id = #{empId} AND del_flag = 0")
    String selectEmployeeName(@Param("empId") Long empId);
}
