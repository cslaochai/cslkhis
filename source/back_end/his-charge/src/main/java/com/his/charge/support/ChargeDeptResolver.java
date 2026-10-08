package com.his.charge.support;

import com.his.charge.api.AppointGateway;
import com.his.charge.api.EmrGateway;
import com.his.charge.api.PatientGateway;
import com.his.common.enums.PaymentItemTypeEnum;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 收费明细 → 开单科室的**唯一**反查出口。
 */
@Component
@RequiredArgsConstructor
public class ChargeDeptResolver {

    private final AppointGateway appointGateway;
    private final EmrGateway emrGateway;
    private final PatientGateway patientGateway;

    /**
     * 按明细类型 + 来源单号反查开单科室。
     *
     * @return 查不到返回 {@code null}（调用方应保持 dept 为空，不要兜底）
     */
    public DeptRef resolve(Integer itemType, String sourceNo) {
        if (itemType == null || !TextUtil.hasText(sourceNo)) {
            return null;
        }
        PaymentItemTypeEnum type = PaymentItemTypeEnum.getByCode(itemType);
        if (type == null) {
            return null;
        }
        return switch (type) {
            case REGISTRATION_FEE -> fromAppoint(sourceNo);
            case WESTERN_MEDICINE, CHINESE_PATENT_MEDICINE, CHINESE_HERBAL_MEDICINE -> fromPrescription(sourceNo);
            case EXAMINATION -> fromInspection(sourceNo);
            case LABORATORY_TEST -> fromLaboratory(sourceNo);
            case TREATMENT -> fromInpatientOrder(sourceNo);
            // 8-耗材材料：来源单号是溯源台账码（HV…），不在任何医嘱/申请单表里，无从反查 ——
            // 科室由高值计费网关按"使用科室快照"直填，这里保持不覆盖口径
            case CONSUMABLE -> null;
        };
    }

    private DeptRef fromAppoint(String registNo) {
        return appointGateway.findDeptByRegistNo(registNo);
    }

    // 各来源单

    private DeptRef fromPrescription(String prescriptionNo) {
        return emrGateway.findDeptByPrescriptionNo(prescriptionNo);
    }

    private DeptRef fromInspection(String applyNo) {
        return emrGateway.findDeptByInspectionApplyNo(applyNo);
    }

    private DeptRef fromLaboratory(String applyNo) {
        return emrGateway.findDeptByLaboratoryApplyNo(applyNo);
    }

    /**
     * 住院医嘱类的明细来源。
     *
     * <p>医嘱单上有科室ID（开立科室快照），是住院收入归科的准确依据。
     * 注意用的是**开立科室**不是患者当前科室 —— 转科后开的医嘱该算新科室。
     */
    private DeptRef fromInpatientOrder(String orderNo) {
        return patientGateway.findDeptByOrderNo(orderNo);
    }

    /**
     * 科室引用（ID + 名称快照）。
     */
    public record DeptRef(Long deptId, String deptName) {
        public boolean isEmpty() {
            return deptId == null && !TextUtil.hasText(deptName);
        }
    }
}
