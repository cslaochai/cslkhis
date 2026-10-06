package com.his.charge.support;

import com.his.charge.service.AppointGateway;
import com.his.charge.service.EmrGateway;
import com.his.charge.service.PatientGateway;
import com.his.common.enums.PaymentItemTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 收费明细 → 开单科室的**唯一**反查出口。
 *
 * <p>为什么要有这个类：科室收入对账（G8 第三级）要求"每一分钱都能说出是哪个科开出来的"，
 * 而旧收费明细自身没有科室列 —— 它是**派生表**，行由来源单生成，
 * 科室只能回到来源单去问。反查规则散在收费单生成的各处（his-emr 综合单、his-appoint 挂号费、
 * his-patient 住院医嘱计费、his-charge 手工建单）会各有各的写法，迟早对不上。
 * 收口到这里，四处共用一套规则。
 *
 * <p><b>按 {@code itemType} 分流</b>（规则来自各来源单在库里的真实字段，不是猜的）：
 * <table border="1">
 *   <tr><th>itemType</th><th>来源表</th><th>关联键</th></tr>
 *   <tr><td>1 挂号费</td><td>挂号信息</td><td>regist_no = sourceNo</td></tr>
 *   <tr><td>2/3/4 药品</td><td>处方主表</td><td>prescription_no = sourceNo</td></tr>
 *   <tr><td>5 检查</td><td>检查申请单</td><td>apply_no = sourceNo</td></tr>
 *   <tr><td>6 检验</td><td>检验申请单</td><td>apply_no = sourceNo</td></tr>
 *   <tr><td>7 治疗 / 其他</td><td>住院医嘱主表</td><td>order_no = sourceNo</td></tr>
 * </table>
 *
 * <p>⚠ 全部用**单号**（{@code sourceNo}）而不是 ID 关联，因为 {@code sourceId} 在各来源语义不统一：
 * 药品明细的 {@code sourceId} 是**处方明细ID**（不是处方ID），检查/检验的是申请单ID，
 * 挂号费的是挂号单ID —— 拿 ID 去问处方主表会一条也查不到。
 * 单号是业务唯一键，语义跨来源一致。
 *
 * <p>⚠ <b>查不到就返回 {@code null}，绝不兜底成某个默认科室</b>：
 * 兜底成"其他科"会让科室收入表凭空多出一块来路不明的钱，
 * 比在页面上单列一条「无科室归属」差异项糟糕得多。
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
        if (itemType == null || !StringUtils.hasText(sourceNo)) {
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
     *
     * <p>名称随行带走而不是只带 ID 再联表回显：科室改名后历史单据上的科室名**不应该跟着变**，
     * 财务凭证要的是"当时是哪个科"。
     */
    public record DeptRef(Long deptId, String deptName) {
        public boolean isEmpty() {
            return deptId == null && !StringUtils.hasText(deptName);
        }
    }
}
