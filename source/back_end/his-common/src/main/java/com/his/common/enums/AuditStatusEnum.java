package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举（适用于病历、报告等单据的审核流转）
 */
@Getter
@AllArgsConstructor
public enum AuditStatusEnum {

    /**
     * 0-待提交
     */
    PENDING_SUBMIT(0, "待提交"),

    /**
     * 1-待审核
     */
    PENDING_AUDIT(1, "待审核"),

    /**
     * 2-审核通过
     */
    APPROVED(2, "审核通过"),

    /**
     * 3-审核驳回
     */
    REJECTED(3, "审核驳回"),

    /**
     * 未知状态（兜底处理，防止解析异常）
     */
    UNKNOWN(-1, "未知状态");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String label;

    /**
     * 根据状态码获取对应的枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，若未匹配则返回 UNKNOWN
     */
    public static AuditStatusEnum fromCode(int code) {
        for (AuditStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }
}