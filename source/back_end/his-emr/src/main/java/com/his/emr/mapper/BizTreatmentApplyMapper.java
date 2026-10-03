package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizTreatmentApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface BizTreatmentApplyMapper extends BaseMapper<BizTreatmentApply> {

    /**
     * 挂号快照读（开单时取患者/科室/医生，作为收费明细的来源依据）。
     *
     * <p>用原生 SQL 而不是 his-appoint 的 Mapper：治疗站在 his-emr，跨模块只读一张表，
     * 引对方实体反而把依赖方向搞乱（与 G21 {@code BizExamDeviceMapper.selectEquipmentOptions} 同口径）。
     *
     * <p>科室ID 是**开单科室**：挂号挂在哪科，这笔治疗费就归哪科。
     * 列名一律按挂号信息的真实字段（该表没有 visit_id）。
     */
    @Select("SELECT a.id AS registId, a.regist_no AS registNo, a.patient_id AS patientId, "
            + "       a.patient_no AS patientNo, a.patient_name AS patientName, "
            + "       a.dept_id AS deptId, a.dept_name AS deptName, "
            + "       a.doctor_id AS doctorId, a.doctor_name AS doctorName, "
            + "       a.regist_status AS registStatus, a.refund_time AS refundTime "
            + "  FROM biz_appoint_info a WHERE a.id = #{registId} AND a.del_flag = 0")
    Map<String, Object> selectRegistSnapshot(@Param("registId") Long registId);
}
