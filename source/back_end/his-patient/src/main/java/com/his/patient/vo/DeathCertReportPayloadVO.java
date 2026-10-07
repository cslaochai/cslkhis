package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 死因监测上报报文（对应 {@code DeathCertificateServiceImpl#buildReportPayload}）。
 *
 * <p><b>报文即契约</b>：字段名按《居民死亡医学证明（推断）书》调查记录逐项对齐，
 * 死因链按 Ⅰ(a~d)/Ⅱ 分组带上，回执侧要的就是这一份。真实对接疾控平台时，
 * 按平台规范替换本类即可，上报流程（建卡 → 审核 → 报送）不变。
 *
 * <p>三段嵌套（underlyingCause / causeChainPartI+II / relative）各自建类而不用嵌套 Map：
 * 嵌套 Map 的键名编译器不管，写错要等疾控侧返回错误报文才发现。
 *
 * <p>时间字段保持 String：报文侧要的是 {@code yyyy-MM-dd HH:mm:ss} 文本，
 * 且这份 JSON 已落库（report_payload），改形状会让历史报文与新报文对不上。
 */
@Data
public class DeathCertReportPayloadVO implements Serializable {

    /**
     * 报文类型（固定 DEATH_CERT_REPORT）
     */
    private String msgType;

    /**
     * 死亡证明编号
     */
    private String certNo;

    /**
     * 患者姓名
     */
    private String name;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 民族
     */
    private String nation;

    /**
     * 出生日期
     */
    private String birthDate;

    /**
     * 死亡时年龄
     */
    private Integer age;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 职业
     */
    private String occupation;

    /**
     * 婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）
     *
     * <p>报码值不报文案：报文要的是可机器判读的口径。
     */
    private Integer maritalStatus;

    /**
     * 死亡时间
     */
    private String deathTime;

    /**
     * 死亡地点（1-院内 2-院外）
     */
    private Integer deathPlace;

    /**
     * 死亡科室名称（院外死亡为空）
     */
    private String deathDept;

    /**
     * 临床诊断
     */
    private String clinicalDiagnosis;

    /**
     * 根本死因
     */
    private UnderlyingCause underlyingCause;

    /**
     * 死因链 Ⅰ 部分（a~b 直接死因、c→d 根本死因）
     */
    private List<CauseItem> causeChainPartI;

    /**
     * 死因链 Ⅱ 部分（其他重要疾病）
     */
    private List<CauseItem> causeChainPartII;

    /**
     * 是否尸检（0-否 1-是）
     */
    private Integer autopsyFlag;

    /**
     * 尸检结果
     */
    private String autopsyResult;

    /**
     * 填报人与联系人
     */
    private Relative relative;

    /**
     * 填报医生
     */
    private String physician;

    /**
     * 填报时间
     */
    private String fillTime;

    /**
     * 签发时间
     */
    private String issueTime;

    /**
     * 上报时限（超过即为逾期）
     */
    private String reportDeadline;

    /**
     * 根本死因段。
     */
    @Data
    public static class UnderlyingCause implements Serializable {

        /**
         * 根本死因 ICD-10 编码
         */
        private String icdCode;

        /**
         * 根本死因名称
         */
        private String name;
    }

    /**
     * 死因链条目段。
     */
    @Data
    public static class CauseItem implements Serializable {

        /**
         * 死因链顺位（I(a)(b)...(c)(d) / II 逐条编号）
         */
        private Integer seqNo;

        /**
         * 死因 ICD-10 编码
         */
        private String icdCode;

        /**
         * 死因名称
         */
        private String name;

        /**
         * 致死间隔（如「直接导致」）
         */
        private String interval;
    }

    /**
     * 联系人段（空值按空串上报，不报 null）。
     */
    @Data
    public static class Relative implements Serializable {

        /**
         * 联系人姓名
         */
        private String name;

        /**
         * 与患者关系
         */
        private String relation;

        /**
         * 联系电话
         */
        private String phone;
    }
}
