package com.his.common.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 签名能力概览。
 */
@Data
public class SignatureSummaryVO {

    /**
     * 签名记录总数（含已作废）
     */
    private Long totalSign;

    /**
     * 有效签名数
     */
    private Long validSign;

    /**
     * 已作废签名数
     */
    private Long invalidSign;

    /**
     * 今天新增签名数
     */
    private Long todaySign;

    /**
     * 有效签名中「未校验」的条数
     */
    private Long verifyUnchecked;

    /**
     * 有效签名中「验签失败」的条数（>0 必须有人去看）
     */
    private Long verifyFailed;

    /**
     * 已接入签名的业务类型数
     */
    private Integer bizTypeCount;

    /**
     * 已登记的业务类型文案，如"住院病历 / 门诊病历 / 住院医嘱"
     */
    private String bizTypeTexts;

    private Long certTotal;
    private Long certActive;
    private Long certRevoked;

    /**
     * 自动签发的证书数（信任级别低于人工签发，页面要能看出来）
     */
    private Long certAutoIssued;

    /**
     * 已有有效证书的员工数
     */
    private Long certEmployeeCount;

    /**
     * 各业务对象类型的签名覆盖率。
     *
     * <p>为什么必须有：库里存量病历都是签名能力上线之前归档的，它们没有签名。
     * 只报"有效签名 1234 条"会让页面显得"全院已覆盖"，而事实是那批历史文书的签名
     * **永远补不回来**（补签 = 伪造，不允许）。覆盖率按类型分别列出，
     * `pendingSign` 才有地方显性呈现。
     */
    private List<Coverage> coverages;

    private LocalDateTime lastSignTime;

    /**
     * 时间来源说明（本期为本机时钟，页面必须原样展示）
     */
    private String timeSourceNote;

    /**
     * TSA 适配器是否在线
     */
    private Boolean tsaAvailable;

    /**
     * TSA 适配器名称
     */
    private String tsaName;

    /**
     * 时间戳令牌台账条数
     */
    private Long tsaTokenCount;

    /**
     * 证书信任级别说明
     */
    private String certTrustNote;

    /**
     * 其余说明
     */
    private List<String> notes;

    @Data
    public static class Coverage {
        /**
         * 业务类型
         */
        private Integer bizType;
        private String bizTypeText;
        /**
         * 对象总数（未删除）
         */
        private Long total;
        /**
         * 已签名
         */
        private Long signed;
        /**
         * 未签名（历史存量主要落在这里）
         */
        private Long pendingSign;
        /**
         * 签名已失效
         */
        private Long invalidated;
        /**
         * 覆盖率文案，分母为 0 时为 "—"（不写成 0%，也不写成 100%）
         */
        private String signedRateText;
    }
}
