package com.his.charge.service;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 住院欠费管控扩展点
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
