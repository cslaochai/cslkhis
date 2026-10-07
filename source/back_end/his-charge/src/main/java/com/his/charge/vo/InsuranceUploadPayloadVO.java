package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 医保 2304 上传报文（{@code InsuranceSettlementServiceImpl#buildUploadPayload}）。
 *
 * <p>为什么拆成 5 个嵌套结构：医保要<b>按项目核费用</b>，整单报一个总金额过不去，
 * 所以报文体是「账单头（patient/visit）+ 金额分组（fees/fund）+ 逐行 items」三段。
 * 用嵌套静态类而不是嵌套 {@code Map}，是为了让"这一段有哪些字段"在编译期就定死。
 *
 * <p>金额一律 BigDecimal：报文的 {@code fees}/{@code fund} 两段全是钱，
 * 经过一次 double 就会在对账时差几分钱，而医保局不会接受"四舍五入到分"的解释。
 *
 * <p>本类是 G7 样例报文（真实对接时按医保前置机 2304 规范做字段映射，
 * 替换 {@code InsuranceChannelService} 即可）。<b>字段声明顺序即 JSON 键顺序，
 * 键名是对外契约，不得改动。</b>
 */
@Data
public class InsuranceUploadPayloadVO implements Serializable {

    /**
     * 报文类型，固定 2304（上传）
     */
    private String msgType;

    /**
     * 本次交易流水号
     */
    private String tradeNo;

    /**
     * 定点医疗机构编码（G7 样例值）
     */
    private String fixMedinsCode;

    /**
     * 定点医疗机构名称（G7 样例值）
     */
    private String fixMedinsName;

    /**
     * 结算清单号
     */
    private String settlementNo;

    /**
     * 发送时间（{@code yyyy-MM-dd HH:mm:ss}）
     */
    private String sendTime;

    /**
     * 患者身份段
     */
    private Patient patient;

    /**
     * 就诊段
     */
    private Visit visit;

    /**
     * 费用分类段（按费用类型拆开的金额）
     */
    private Fees fees;

    /**
     * 支付构成段（统筹/个账/自费）
     */
    private Fund fund;

    /**
     * 费用明细行（医保按行核费用，整单一个数报不出去）
     */
    private List<Item> items;

    /**
     * 备注（G7 样例报文说明）
     */
    private String note;

    /**
     * 患者身份段
     */
    @Data
    public static class Patient implements Serializable {

        /**
         * 患者号
         */
        private String patientNo;

        /**
         * 患者姓名
         */
        private String patientName;

        /**
         * 性别（1-男 2-女）
         */
        private Integer gender;

        /**
         * 年龄
         */
        private Integer age;

        /**
         * 身份证号
         */
        private String idCard;

        /**
         * 医保号
         */
        private String insuranceNo;

        /**
         * 参保类型
         */
        private String insuranceType;
    }

    /**
     * 就诊段
     */
    @Data
    public static class Visit implements Serializable {

        /**
         * 账单号
         */
        private String billNo;

        /**
         * 就诊类型（人读文案，由 {@code EncounterTypeEnum#descOf} 转好再入报文）
         */
        private String encounterType;

        /**
         * 门诊/住院类别
         */
        private String visitType;

        /**
         * 科室名称
         */
        private String deptName;

        /**
         * 医生姓名
         */
        private String doctorName;

        /**
         * 诊断编码（ICD）
         */
        private String diagnosisCode;

        /**
         * 诊断名称（结构化诊断优先，空则回落诊断文本）
         */
        private String diagnosisName;
    }

    /**
     * 费用分类段：按费用类型拆开，让医保能分别核对药品/检查/检验的占比
     */
    @Data
    public static class Fees implements Serializable {

        /**
         * 费用总额（元）
         */
        private BigDecimal total;

        /**
         * 药品费（元）
         */
        private BigDecimal drug;

        /**
         * 检查费（元）
         */
        private BigDecimal inspection;

        /**
         * 检验费（元）
         */
        private BigDecimal laboratory;

        /**
         * 治疗费（元）
         */
        private BigDecimal treatment;

        /**
         * 材料费（元）
         */
        private BigDecimal material;

        /**
         * 其他费（元）
         */
        private BigDecimal other;
    }

    /**
     * 支付构成段：医保付多少、个账扣多少、患者自付多少
     */
    @Data
    public static class Fund implements Serializable {

        /**
         * 报销比例
         */
        private BigDecimal coverageRatio;

        /**
         * 统筹支付（元）
         */
        private BigDecimal insurancePay;

        /**
         * 个人账户支付（元）
         */
        private BigDecimal personalPay;

        /**
         * 自付（元）
         */
        private BigDecimal selfPay;
    }

    /**
     * 费用明细行（逐行 split 的结果，是医保核费用的最小单位）
     */
    @Data
    public static class Item implements Serializable {

        /**
         * 费用项目类型
         */
        private Integer itemType;

        /**
         * 费用项目类型名（人读文案）
         */
        private String itemTypeName;

        /**
         * 项目编码
         */
        private String itemCode;

        /**
         * 项目名称
         */
        private String itemName;

        /**
         * 规格
         */
        private String specification;

        /**
         * 单位
         */
        private String unit;

        /**
         * 数量
         */
        private BigDecimal quantity;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 行金额（元）
         */
        private BigDecimal amount;

        /**
         * 优惠金额（元，院内优惠/抹零）
         */
        private BigDecimal discount;

        /**
         * 医保目录类别（0-自费 1-甲类 2-乙类 3-丙类）
         */
        private Integer catalogType;

        /**
         * 医保拆分：统筹支付（元）
         */
        private BigDecimal pool;

        /**
         * 医保拆分：自费（元）
         */
        private BigDecimal self;
    }
}