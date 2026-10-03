package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.InpatientTransferQueryPageDTO;
import com.his.patient.entity.BizInpatientTransfer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 住院转科轨迹 Mapper。
 *
 * <p>自定义 {@code @Select} 不受 {@code @TableLogic} 影响 → 必须显式写 {@code del_flag = 0}。
 */
@Mapper
public interface BizInpatientTransferMapper extends BaseMapper<BizInpatientTransfer> {

    /**
     * 转科记录分页（返回实体；科室名/床号都是快照列，不需要 JOIN）
     */
    @Select("""
            SELECT t.*
            FROM biz_inpatient_transfer t
            WHERE t.del_flag = 0
              AND (#{q.admissionId} IS NULL OR t.admission_id = #{q.admissionId})
              AND (#{q.patientId} IS NULL OR t.patient_id = #{q.patientId})
              AND (#{q.fromDeptId} IS NULL OR t.from_dept_id = #{q.fromDeptId})
              AND (#{q.toDeptId} IS NULL OR t.to_dept_id = #{q.toDeptId})
              AND (#{q.transferStatus} IS NULL OR t.transfer_status = #{q.transferStatus})
              AND (#{q.transferType} IS NULL OR t.transfer_type = #{q.transferType})
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR t.transfer_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR t.admission_no LIKE CONCAT('%', #{q.keyword}, '%')
                   OR t.patient_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR t.transfer_reason LIKE CONCAT('%', #{q.keyword}, '%'))
            ORDER BY t.apply_time DESC, t.id DESC
            """)
    IPage<BizInpatientTransfer> selectTransferPage(IPage<BizInpatientTransfer> page,
                                                  @Param("q") InpatientTransferQueryPageDTO query);

    /**
     * 某次住院的全部转科轨迹（按发生顺序升序：第一条的 from_dept 就是入院科室）
     */
    @Select("""
            SELECT t.* FROM biz_inpatient_transfer t
            WHERE t.del_flag = 0 AND t.admission_id = #{admissionId}
            ORDER BY t.apply_time ASC, t.id ASC
            """)
    List<BizInpatientTransfer> selectByAdmission(@Param("admissionId") Long admissionId);

    /**
     * 当天已生成的转科单号条数（单号序号用）
     */
    @Select("SELECT COUNT(*) FROM biz_inpatient_transfer WHERE del_flag = 0 AND transfer_no LIKE CONCAT(#{prefix}, '%')")
    long countByNoPrefix(@Param("prefix") String prefix);

    /**
     * 待接收转科数（转入科室工作台角标用）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_inpatient_transfer
            WHERE del_flag = 0 AND transfer_status = 0
              AND (#{toDeptId} IS NULL OR to_dept_id = #{toDeptId})
              AND (#{admissionId} IS NULL OR admission_id = #{admissionId})
            """)
    long countPending(@Param("toDeptId") Long toDeptId, @Param("admissionId") Long admissionId);

    /**
     * 科室名（快照写入用；查不到就返回 null，由服务层渲染「未知科室(ID=x)」而不是编一个名字）
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    /**
     * 某次住院是否存在待接收的转科（防止重复发起）
     */
    @Select("""
            SELECT COUNT(*) FROM biz_inpatient_transfer
            WHERE del_flag = 0 AND admission_id = #{admissionId} AND transfer_status = 0
            """)
    long countPendingByAdmission(@Param("admissionId") Long admissionId);
}
