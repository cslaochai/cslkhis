package com.his.common.support;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 急诊诊查费政策辅助（国家医疗服务价格项目 110200003「急诊诊察费」）。
 *
 * <p><b>价格已迁价表</b>：全额与档位维护在 sys_charge_policy / sys_charge_policy_price
 * （政策编码 EMG_CONSULT，价格管理页「政策价」页签可调价），运行时经
 * {@code ChargeFacadeService.policyPrice(POLICY_EMG_CONSULT, tier)} 实时取价。
 * 本类只保留与价格无关的政策表达：儿童加收口径与费用行备注文案。
 *
 * <p><b>档位口径</b>：急诊按班接诊、没有号源快照，「知名专家」名单制档在急诊不适用，
 * 主任医师在急诊按主任档收。职称 → 档位映射见 {@link TitleTierMapping#tierOf}。
 *
 * <p><b>收费时机（先救治后付费）</b>：急诊挂号/接诊时<b>不收款不拦人</b>，费用行在
 * 接诊时刻记账（biz_fee_record 待结算），随该患者后续账单一并出单；到收费台缴纳。
 * 患者未缴费不影响任何诊疗环节——账单挂待缴，长期未缴走挂账欠费口径（bill_status=6）。
 */
public final class EmergencyConsultFeePolicy {

    private EmergencyConsultFeePolicy() {
    }

    /**
     * 应收急诊诊查费（含儿童加收）。
     *
     * @param baseAmount 价表全额（EMG_CONSULT 本档 amount）
     * @param birthDate  患者出生日期，可为空（判不出儿童就不加收，同门诊口径）
     * @param visitDate  就诊日（按就诊日算年龄，不按登记日）
     */
    public static BigDecimal payable(BigDecimal baseAmount, LocalDate birthDate, LocalDate visitDate) {
        return PediatricSurchargePolicy.apply(baseAmount, birthDate, visitDate);
    }

    /**
     * 费用行备注：写清政策依据与档位，供收费台/审计追溯「这 23 块是怎么来的」。
     *
     * @param tierLabel  价表档位名（如「主任医师」）
     * @param baseAmount 价表全额（儿童加收额按它计算，写进备注）
     */
    public static String remarkOf(String tierLabel, BigDecimal baseAmount,
                                  LocalDate birthDate, LocalDate visitDate) {
        StringBuilder sb = new StringBuilder("急诊诊查费（").append(tierLabel).append("档");
        if (PediatricSurchargePolicy.isChild(birthDate, visitDate)) {
            sb.append("，儿童加收30%：").append(PediatricSurchargePolicy.surchargeOf(baseAmount).toPlainString());
        }
        sb.append("；依据国家价格项目110200003，先救治后付费）");
        return sb.toString();
    }
}
