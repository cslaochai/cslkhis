package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 住院病历结构化率统计 VO。
 *
 * <p>三个层次一起给，是因为"结构化率 76%"本身不可行动：
 * <ul>
 *   <li><b>总体</b>：{@code elementFilled / elementTotal} —— 报表口径；</li>
 *   <li><b>分组</b>：病史 / 生命体征 / 体格检查 / 诊疗结论 —— 定位"是缺在查体还是缺在病史"；</li>
 *   <li><b>逐条要素 + 逐份文书</b>：{@code elements} 与 {@code records[].missingLabels}
 *       —— 定位到"哪一份病历缺腹部查体"。P5 数据质量报表要的就是这一层（不达标必须能落到具体记录）。</li>
 * </ul>
 *
 * <p>{@code elementTotal} 是**按每份文书自己的类型分别累加**得到的（病程里才有病程正文），
 * 所以它不等于 {@code recordCount × 26}。
 */
@Data
public class RecordQualityStatVO implements Serializable {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 纳入统计的文书份数
     */
    private Integer recordCount;

    /**
     * 应填结构化要素总数（分母）
     */
    private Integer elementTotal;

    /**
     * 已填结构化要素总数
     */
    private Integer elementFilled;

    /**
     * 结构化率（百分数，2 位小数；无文书时为 null）
     */
    private BigDecimal structuredRate;

    /**
     * 结构化率文案（如 "81.48%"；无文书时为 "—"）
     */
    private String structuredRateText;

    /**
     * 分组统计
     */
    private List<GroupStatVO> groups;

    /**
     * 逐要素统计
     */
    private List<ElementStatVO> elements;

    /**
     * 逐份文书的明细（不达标能定位到具体记录）
     */
    private List<RecordStatVO> records;

    /**
     * 分组统计
     */
    @Data
    public static class GroupStatVO implements Serializable {

        /**
         * 分组编码
         */
        private String group;

        /**
         * 分组中文名
         */
        private String groupLabel;

        /**
         * 应填数
         */
        private Integer elementTotal;

        /**
         * 已填数
         */
        private Integer elementFilled;

        /**
         * 分组填充率（百分数，2 位）
         */
        private BigDecimal rate;

        /**
         * 分组填充率文案
         */
        private String rateText;
    }

    /**
     * 逐要素统计
     */
    @Data
    public static class ElementStatVO implements Serializable {

        /**
         * 要素编码（= 库列名）
         */
        private String code;

        /**
         * 要素中文名
         */
        private String label;

        /**
         * 分组编码
         */
        private String group;

        /**
         * 分组中文名
         */
        private String groupLabel;

        /**
         * 已填文书数
         */
        private Integer filledCount;

        /**
         * 缺失文书数
         */
        private Integer missingCount;

        /**
         * 填充率（百分数，2 位）
         */
        private BigDecimal rate;

        /**
         * 填充率文案
         */
        private String rateText;
    }

    /**
     * 逐份文书统计
     */
    @Data
    public static class RecordStatVO implements Serializable {

        /**
         * 文书ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long recordId;

        /**
         * 文书号
         */
        private String recordNo;

        /**
         * 文书类型码
         */
        private Integer recordType;

        /**
         * 文书类型文案
         */
        private String recordTypeText;

        /**
         * 文书状态文案
         */
        private String recordStatusText;

        /**
         * 已填数
         */
        private Integer filled;

        /**
         * 总条数
         */
        private Integer total;

        /**
         * 结构化率（百分数，2 位）
         */
        private BigDecimal rate;

        /**
         * 结构化率文案
         */
        private String rateText;

        /**
         * 缺失要素中文名
         */
        private List<String> missingLabels;
    }
}
