package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizInfectiousReport;
import com.his.emr.vo.DeptSnapshotVO;
import com.his.emr.vo.PatientSnapshotVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 传染病报卡 Mapper。
 *
 * <p>跨模块读（患者档案 / 挂号单）走裸 SQL，不引入模块依赖；列名以 information_schema 实查为准。
 */
@Mapper
public interface BizInfectiousReportMapper extends BaseMapper<BizInfectiousReport> {

    /**
     * 按编号查（含软删行）——报卡编号有唯一键，查重必须含软删行，否则删过的号重生会撞唯一键。
     */
    @Select("SELECT COUNT(*) FROM biz_infectious_report WHERE report_no = #{reportNo}")
    int countByNoIncludingDeleted(@Param("reportNo") String reportNo);

    /**
     * 患者快照（编号/姓名/性别/年龄）：报卡是法定凭证，档案改名不影响历史报卡。
     */
    @Select("SELECT id AS patientId, patient_no AS patientNo, patient_name AS patientName,"
            + " gender, age FROM biz_patient WHERE id = #{patientId} AND del_flag = 0")
    PatientSnapshotVO selectPatientSnapshot(@Param("patientId") Long patientId);

    /**
     * 门诊挂号单科室（发现科室兜底来源）。
     */
    @Select("SELECT dept_id AS deptId, dept_name AS deptName FROM biz_appoint_info"
            + " WHERE id = #{registId} AND del_flag = 0")
    DeptSnapshotVO selectRegistDept(@Param("registId") Long registId);
}