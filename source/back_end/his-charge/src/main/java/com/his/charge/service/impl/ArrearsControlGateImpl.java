package com.his.charge.service.impl;


import com.his.charge.entity.BizArrearsPolicy;
import com.his.charge.mapper.BizArrearsPolicyMapper;
import com.his.charge.service.ArrearsControlGate;
import com.his.charge.service.InpatientAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 欠费管控 Gate 实现（SPI：接口在 his-patient，本类是 his-charge 侧实现）。
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArrearsControlGateImpl implements ArrearsControlGate {

    /**
     * 永不拦截的医嘱类别：1-药品 6-手术 7-输血（治疗生命线，与配置无关）
     */
    private static final Set<Integer> NEVER_STOP_CLASSES = Set.of(1, 6, 7);

    private final BizArrearsPolicyMapper bizArrearsPolicyMapper;
    private final InpatientAccountService inpatientAccountService;

    @Override
    public CheckResult checkNewOrder(OrderCheck command) {
        BizArrearsPolicy policy = bizArrearsPolicyMapper.selectById(1L);
        if (policy == null || policy.getStopEnabled() == null || policy.getStopEnabled() != 1) {
            return CheckResult.allow();
        }
        if (policy.getStopLine() == null || command == null || command.getAdmissionId() == null) {
            return CheckResult.allow();
        }
        BigDecimal arrears = inpatientAccountService.arrearsView(command.getAdmissionId()).arrearsAmount();
        if (arrears.compareTo(policy.getStopLine()) < 0) {
            return CheckResult.allow();
        }

        Set<Integer> stopClasses = parseStopClasses(policy.getStopClasses());
        Set<Integer> hit = command.getOrderClasses() == null ? Set.of()
                : command.getOrderClasses().stream()
                .filter(c -> !NEVER_STOP_CLASSES.contains(c))
                .filter(stopClasses::contains)
                .collect(Collectors.toSet());
        if (hit.isEmpty()) {
            return CheckResult.allow();
        }
        String classNames = hit.stream().map(this::className).collect(Collectors.joining("、"));
        return CheckResult.deny(String.format(
                "该患者已欠费 %.2f 元（停费线 %.2f 元），按院方管控策略暂停「%s」类新开医嘱；药品/手术/急救类不受影响，请通知补缴预交金",
                arrears, policy.getStopLine(), classNames));
    }

    private Set<Integer> parseStopClasses(String csv) {
        if (!StringUtils.hasText(csv)) {
            return Set.of(2, 3, 4);
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> s.matches("\\d+"))
                .map(Integer::valueOf).collect(Collectors.toSet());
    }

    private String className(Integer c) {
        return switch (c) {
            case 1 -> "药品";
            case 2 -> "检查";
            case 3 -> "检验";
            case 4 -> "治疗";
            case 5 -> "护理";
            case 6 -> "手术";
            case 7 -> "输血";
            case 8 -> "监护";
            default -> "其他";
        };
    }
}
