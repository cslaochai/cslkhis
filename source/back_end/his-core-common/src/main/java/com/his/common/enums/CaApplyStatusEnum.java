package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * CA 在线申请单状态（sys_sign_cert.ca_apply_status）。
 *
 * <p>前五个与 csssl 对外申请单 status 逐字一致；FAILED 是 HIS 本地提交失败
 * （CA 不可达 / 凭证错误 / 模板不匹配），用于页面提示「可重试」，CA 侧不存在该状态。
 */
public enum CaApplyStatusEnum {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已批准待签发"),
    ISSUED("ISSUED", "已签发"),
    REJECTED("REJECTED", "已驳回"),
    CANCELLED("CANCELLED", "已撤销"),
    FAILED("FAILED", "提交失败");

    private final String code;
    private final String label;

    CaApplyStatusEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CaApplyStatusEnum fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (CaApplyStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(String code) {
        return getLabel(code, StringUtils.EMPTY);
    }

    /**
     * 展示：合法→label；未知码值→defaultValue（CA 新增状态时至少把原值显示出来）
     */
    public static String getLabel(String code, String defaultValue) {
        CaApplyStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
