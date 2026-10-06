package com.his.charge.service;

import lombok.NoArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 住院欠费管控扩展点（由 his-charge 模块提供实现）。
 *
 * <p>依赖方向实测是 {@code his-charge → his-patient}（单向），所以「开新医嘱前查欠费」
 * 定义成接口放在调用方（his-patient），实现放 his-charge。调用侧必须用
 * {@code ObjectProvider<ArrearsControlGate>.getIfAvailable()} 惰性获取：
 * 收费模块缺席时 <b>fail-open 放行</b>（与全局路由守卫同口径）——欠费管控缺位最多是财务纪律松了，
 * 把开药卡死是医疗事故。
 *
 * <p>实现侧安全底线（his-charge 必须遵守）：
 * <ul>
 *   <li>策略未启用（stop_enabled=0）→ 一律放行；</li>
 *   <li>只拦<b>择期类</b>医嘱（检查/检验/治疗），<b>药品（1）、手术（6）、输血（7）、急救相关永不拦截</b>；</li>
 *   <li>欠费计算口径必须与 InpatientAccountService 一致：已发生 = L1 记账行应收净额，
 *       已收 = 净预交 + 账单上直接收的钱（都不许再读旧表旧预交金 / 旧收费单）。</li>
 * </ul>
 */
public interface ArrearsControlGate {

    /**
     * 新开医嘱前的欠费管控校验（只对「新增」调用；修改/停嘱/取消不拦）。
     *
     * @param command 本次医嘱涉及的全部医嘱类别
     * @return 不允许时 reason 给出可直接展示给医生的原因；返回 null 视为放行
     */
    CheckResult checkNewOrder(OrderCheck command);

    /**
     * 校验入参。
     */
    @Data
    @NoArgsConstructor
    class OrderCheck implements Serializable {

        /**
         * 入院ID
         */
        private Long admissionId;

        /**
         * 本次医嘱包含的医嘱类别集合（1-药品 2-检查 3-检验 4-治疗 5-护理 6-手术 7-输血 8-监护 9-其他 10-临床营养）
         */
        private Set<Integer> orderClasses;

        public OrderCheck(Long admissionId, Set<Integer> orderClasses) {
            this.admissionId = admissionId;
            this.orderClasses = orderClasses;
        }
    }

    /**
     * 校验结果。
     */
    @Data
    class CheckResult implements Serializable {

        /**
         * 是否允许开单
         */
        private boolean allowed;

        /**
         * 拦截原因（allowed=false 时必填，直接展示给医生）
         */
        private String reason;

        public static CheckResult allow() {
            CheckResult r = new CheckResult();
            r.setAllowed(true);
            return r;
        }

        public static CheckResult deny(String reason) {
            CheckResult r = new CheckResult();
            r.setAllowed(false);
            r.setReason(reason);
            return r;
        }
    }
}
