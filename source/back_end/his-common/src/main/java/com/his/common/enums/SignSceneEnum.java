package com.his.common.enums;

/**
 * 签名场景：说明"这一次签名是因为什么业务动作产生的"。
 *
 * <p>同一个对象可以有多次签名（医嘱双签、作废后补签），靠场景区分是谁在哪个环节签的。
 */
public enum SignSceneEnum {

    /**
     * 病历提交时，书写医生签名
     */
    SUBMIT(1, "提交签名"),

    /**
     * 病历归档时补签（仅当提交时未签过；归档签名人不一定是书写医生）
     */
    ARCHIVE(2, "归档签名"),

    /**
     * 医嘱开立时，开立医生签名
     */
    ORDER_CREATE(3, "开立签名"),

    /**
     * 医嘱校对时，校对护士签名
     */
    ORDER_VERIFY(4, "校对签名"),

    /**
     * 事后补签（管理员在签名中心发起，必须写明原因）
     */
    MAKEUP(5, "补签"),

    /**
     * 处方开立时，开方医师签名
     */
    RX_CREATE(6, "开方签名"),

    /**
     * 处方审核时，审方药师签名（《处方管理办法》要求审方由药师完成，与发药是两道手）
     */
    RX_AUDIT(7, "审方签名"),

    /**
     * 检查/检验报告出结果时，报告医师签名
     */
    REPORT_ISSUE(8, "报告签名"),

    /**
     * 检查/检验报告审核时，审核医师签名
     */
    REPORT_AUDIT(9, "报告审核签名"),

    /**
     * 检查/检验申请单开立（送检提交）时，开单医师签名
     */
    APPLY_CREATE(10, "申请开立签名"),

    /**
     * 病危重通知签发时，告知医师电子签名（法定告知落款，sql/161）
     */
    NOTICE_ISSUE(11, "告知签发签名"),

    /**
     * 病危重通知作废签名后的补签（内容冻结后重新落款）
     */
    NOTICE_MAKEUP(12, "告知补签"),

    /**
     * 住院请假单医师批准时电子签名（放行决定的责任落款，sql/162）
     */
    LEAVE_APPROVE(13, "请假审批签名");

    private final int code;
    private final String text;

    SignSceneEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static SignSceneEnum parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (SignSceneEnum s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }

    public static String textOf(Integer code) {
        SignSceneEnum s = parse(code);
        if (s != null) {
            return s.text;
        }
        return code == null ? "—" : "未知(" + code + ")";
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
