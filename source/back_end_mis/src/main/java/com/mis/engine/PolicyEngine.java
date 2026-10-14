package com.mis.engine;

import com.mis.config.MisProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 医保政策引擎（演示用的确定性规则）：
 * 1) 2304 平台侧即时核定：按险种 × 门诊/住院 的起付线、报销比例独立核算统筹；
 * 2) 月度智能审核：结算单号末位命中规则数字即判违规（模拟超限价/不合理诊疗）。
 */
@Component
public class PolicyEngine {

    private final MisProperties props;

    public PolicyEngine(MisProperties props) {
        this.props = props;
    }

    /**
     * 平台独立核定统筹支付（用于 2304 回执，与院方申报值相互独立）
     */
    public BigDecimal confirmPool(String insuranceType, String encounterType, BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        MisProperties.Scheme scheme = isEmployee(insuranceType)
                ? props.getPolicy().getEmployee()
                : props.getPolicy().getResident();
        MisProperties.Rate rate = isInpatient(encounterType)
                ? scheme.getInpatient()
                : scheme.getOutpatient();

        BigDecimal base = total.subtract(rate.getDeductible()).max(BigDecimal.ZERO);
        return base.multiply(rate.getRatio()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 智能审核是否判违规（按结算单号末位数字）
     */
    public boolean isViolation(String settlementNo) {
        if (settlementNo == null || settlementNo.isEmpty()) {
            return false;
        }
        char last = settlementNo.charAt(settlementNo.length() - 1);
        if (!Character.isDigit(last)) {
            return false;
        }
        return props.getAudit().getRefuseTailDigits().contains(last - '0');
    }

    private boolean isEmployee(String insuranceType) {
        String t = insuranceType == null ? "" : insuranceType.trim();
        return t.contains("职工") || "2".equals(t);
    }

    private boolean isInpatient(String encounterType) {
        String t = encounterType == null ? "" : encounterType.trim();
        return t.contains("住院");
    }
}
