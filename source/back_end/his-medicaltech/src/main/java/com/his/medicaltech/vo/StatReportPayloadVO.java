package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 病案统计上报报文（落库到 {@code biz_stat_report.payload}，也是前端预览/打印的数据源）。
 *
 * <p><b>字段名是前后端契约，不能随手改</b>：前端 {@code StatReportView.vue} 按
 * {@code reportKind / reportName / org.* / period.* / scope.* / indicators.*}
 * 与 {@code operationLevels[].levelLabel / insuranceTypes[].type / topDiagnoses[].code /
 * cases[].admissionNo} 这些路径取值，还原成纸质报表。改一个键名 = 预览表格整列空白，
 * 而且不会报错。
 *
 * <p>曾用 {@code LinkedHashMap<String, Object>} 双花括号匿名子类拼这个报文 ——
 * 键名是纯字符串字面量，编译器一个字都不管；而这份 payload 落库后就是<b>对外承诺的内容</b>
 * （可回看、可打印、真实对接时直接换 http 上报），拼错字段名要等疾控侧返回错误报文才发现。
 *
 * <p>各嵌套段（org / period / scope / indicators / 各级明细列表）各自建类，
 * 不用嵌套 Map —— 嵌套 Map 的键名编译器不管，改错了要等对接才发现。
 */
@Data
public class StatReportPayloadVO implements Serializable {

    /**
     * 报文码（WS4-STAT-{报告类型}-{期间}）
     */
    private String reportKind;

    /**
     * 报表名称
     */
    private String reportName;

    /**
     * 机构信息（打印预留，对接真实平台时替换）
     */
    private Org org;

    /**
     * 统计期间
     */
    private Period period;

    /**
     * 统计范围（全院或某科室）
     */
    private Scope scope;

    /**
     * 主要指标
     */
    private Indicators indicators;

    /**
     * 手术级别构成
     */
    private List<OperationLevelItem> operationLevels;

    /**
     * 险种构成
     */
    private List<InsuranceItem> insuranceTypes;

    /**
     * 主要诊断顺位（前 10）
     */
    private List<TopDiagnosisItem> topDiagnoses;

    /**
     * 病例明细（最多 500 条）
     */
    private List<StatCohortCaseRowVO> cases;

    /**
     * 预留说明（打印与对接口径说明）
     */
    private Reserved reserved;

    /**
     * 生成人
     */
    private String operator;

    /**
     * 生成时间
     */
    private String generatedAt;

    /**
     * 机构信息段。
     */
    @Data
    public static class Org implements Serializable {

        /**
         * 机构全称（预留：真实对接时填写）
         */
        private String orgName;

        /**
         * 卫生统计机构代码（预留）
         */
        private String orgCode;

        /**
         * 行政区划代码（预留）
         */
        private String regionCode;
    }

    /**
     * 统计期间段。
     */
    @Data
    public static class Period implements Serializable {

        /**
         * 期间类型（1-月报 2-年报）
         */
        private Integer type;

        /**
         * 期间类型中文名
         */
        private String typeLabel;

        /**
         * 期间值：月报 yyyy-MM，年报 yyyy
         */
        private String value;

        /**
         * 起始时间
         */
        private String start;

        /**
         * 截止时间
         */
        private String end;
    }

    /**
     * 统计范围段。
     */
    @Data
    public static class Scope implements Serializable {

        /**
         * 科室ID；全院口径为 null
         */
        private String deptId;

        /**
         * 科室名；全院口径固定为「全院」
         */
        private String deptName;
    }

    /**
     * 主要指标段。
     */
    @Data
    public static class Indicators implements Serializable {

        /**
         * 出院例数
         */
        private Long dischargeCount;

        /**
         * 死亡例数
         */
        private Long deathCount;

        /**
         * 平均住院日
         */
        private BigDecimal avgLosDays;

        /**
         * 手术台次
         */
        private Long operationCount;

        /**
         * 三级及以上手术台次
         */
        private Long level3upCount;

        /**
         * 出院结算单笔数
         */
        private Long settleCount;

        /**
         * 费用总额
         */
        private BigDecimal totalAmount;

        /**
         * 医保支付
         */
        private BigDecimal insuranceAmount;

        /**
         * 个人自付
         */
        private BigDecimal patientPayAmount;

        /**
         * 欠费
         */
        private BigDecimal arrearsAmount;
    }

    /**
     * 手术级别构成一行。
     */
    @Data
    public static class OperationLevelItem implements Serializable {

        /**
         * 手术级别（0-未录级别）
         */
        private Long level;

        /**
         * 级别中文名（未录级别时为「未录级别」，其余为「N级」）
         */
        private String levelLabel;

        /**
         * 台次
         */
        private Long count;
    }

    /**
     * 险种构成一行。
     */
    @Data
    public static class InsuranceItem implements Serializable {

        /**
         * 险种名称
         */
        private String type;

        /**
         * 结算单笔数
         */
        private Long count;

        /**
         * 金额
         */
        private BigDecimal amount;
    }

    /**
     * 主要诊断顺位一行。
     */
    @Data
    public static class TopDiagnosisItem implements Serializable {

        /**
         * ICD 编码
         */
        private String code;

        /**
         * 诊断名称
         */
        private String name;

        /**
         * 例数
         */
        private Long count;
    }

    /**
     * 预留说明段。
     */
    @Data
    public static class Reserved implements Serializable {

        /**
         * 报送渠道说明（打印预留，未对接外部平台）
         */
        private String sendChannel;

        /**
         * 回执与对账口径说明
         */
        private String receipt;

        /**
         * 打印说明
         */
        private String printTip;
    }
}