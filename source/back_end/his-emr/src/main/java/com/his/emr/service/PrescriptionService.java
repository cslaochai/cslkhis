package com.his.emr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.emr.dto.InspectionApplyQueryDTO;
import com.his.emr.dto.LaboratoryApplyQueryDTO;
import com.his.emr.dto.PrescriptionAuditDTO;
import com.his.emr.dto.PrescriptionQueryDTO;
import com.his.emr.dto.PrescriptionQueryPageDTO;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.entity.BizLaboratoryApply;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.vo.BizPrescriptionVO;
import com.his.emr.vo.MyPrescriptionVO;
import com.his.emr.vo.PrescriptionRationalVO;

import java.util.List;

/**
 * 医生工作站服务接口
 */
public interface PrescriptionService extends IService<BizMedicalRecord> {

    /**
     * 查询处方列表（不分页）
     */
    List<BizPrescriptionVO> getByPatientId(PrescriptionQueryDTO queryDTO);

    /**
     * 患者端·我的处方（按就诊人查全部处方，含药味明细与状态文案，不含草稿）
     */
    List<MyPrescriptionVO> myPrescriptions(Long patientId);

    /**
     * 处方分页查询（含明细与签名锚点）；审方工作台默认只看未审方（{@code unauditedOnly}）
     */
    PageResult<BizPrescriptionVO> listPage(PrescriptionQueryPageDTO query);

    /**
     * 处方审核（审方药师签名 + 状态置「已审核」）。
     *
     * <p>审核人由调用方从登录态取（{@code CurrentUser.employeeId}），不接受入参传入。
     */
    BizPrescriptionVO auditPrescription(PrescriptionAuditDTO dto, Long auditorId, String auditorName,
                                        Long auditorDeptId, String auditorDeptName);

    /**
     * 合理用药批量审查（药物相互作用 × 剂量上限，知识表在药品字典* 两张）
     *
     * <p>只读、不落库：给审方工作台整页标注用。命中禁忌级时 {@code blocked=true}，
     * 此时 {@link #auditPrescription} 点「通过」会被服务端拒绝 —— 提示与拦阻共用一个算法，
     * 不允许出现「界面说能过、后端不给过」两套口径。
     */
    List<PrescriptionRationalVO> rationalCheck(List<Long> prescriptionIds);

    /**
     * 查询检查申请列表（不分页）
     */
    List<BizInspectionApply> getByPatientId(InspectionApplyQueryDTO queryDTO);
    /**
     * 查询检验申请列表（不分页）
     */
    List<BizLaboratoryApply> getByPatientId(LaboratoryApplyQueryDTO queryDTO);
}
