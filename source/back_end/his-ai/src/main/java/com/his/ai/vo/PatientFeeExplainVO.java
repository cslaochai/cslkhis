package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 患者端费用解释结果。
 * <p>
 * 回答一个问题：「这笔钱是怎么算出来的，为什么我要自己掏这么多」。
 * 全部字段都是确定性计算（账单金额、明细按目录类别与项目类型汇总、summary 规则拼接），
 * <b>不经过大模型</b> —— 拆分与说明可穷举成对照表（纪律 9），且解释的是钱，幻觉不可接受，
 * 因此没有 degraded/degradeReason 语义。
 * <p>
 * <b>不解释医保政策</b>：统筹比例、起付线、封顶线各地各险种都不同，
 * 且会变。系统只说账单上实际发生了什么（甲类多少、乙类多少、自费多少），
 * 政策细节一律引导到窗口或医保经办 —— 这与 FAQ 语料的同一条纪律一致。
 */
@Data
@Schema(description = "患者端费用解释结果")
public class PatientFeeExplainVO {

    @Schema(description = "账单ID（字符串）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;

    @Schema(description = "账单号")
    private String billNo;

    @Schema(description = "就诊人姓名")
    private String patientName;

    @Schema(description = "账单日期")
    private LocalDate billDate;

    @Schema(description = "账单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "医保统筹支付")
    private BigDecimal poolAmount;

    @Schema(description = "个人账户支付")
    private BigDecimal accountAmount;

    @Schema(description = "个人自付（现金/微信等）")
    private BigDecimal selfAmount;

    @Schema(description = "医保类型（城镇职工医保…）；自费结算时为 null")
    private String insuranceType;

    @Schema(description = "统筹报销比例（%）；自费结算时为 null")
    private BigDecimal coverageRatio;

    @Schema(description = "自付占总费用比例（百分数，保留 1 位）")
    private BigDecimal selfRatio;

    @Schema(description = "按医保目录类别的分组汇总（自付多少的答案在这里）")
    private List<FeeCatalogGroupVO> catalogGroups;

    @Schema(description = "按项目类型的分组汇总（药品/检查/检验/治疗…）")
    private List<FeeItemGroupVO> itemGroups;

    @Schema(description = "自付金额最高的前几项（患者最容易对这几项有疑问）")
    private List<FeeTopItemVO> topSelfItems;

    @Schema(description = "核心结论：为什么自付这么多（规则生成）")
    private String reasonText;

    @Schema(description = "一句话总结（规则拼接：总额/统筹/自付/占比最高的目录类别）")
    private String summary;

    @Schema(description = "固定提示：以窗口/医保经办解释为准")
    private String advice;

    /**
     * 按项目类型分组。
     */
    @Data
    @Schema(description = "费用解释-项目类型分组")
    public static class FeeItemGroupVO {

        /**
         * 1-挂号费 2-西药 3-中成药 4-中药饮片 5-检查 6-检验 7-治疗 8-耗材
         */
        @Schema(description = "项目类型")
        private Integer itemType;

        @Schema(description = "类型文案")
        private String itemTypeText;

        @Schema(description = "金额合计")
        private BigDecimal amount;

        @Schema(description = "项目数")
        private Integer itemCount;
    }

    /**
     * 自付金额最高的明细项。
     */
    @Data
    @Schema(description = "费用解释-自付 top 明细")
    public static class FeeTopItemVO {

        @Schema(description = "项目名称")
        private String itemName;

        @Schema(description = "金额")
        private BigDecimal amount;

        @Schema(description = "医保目录类别文案")
        private String catalogText;

        @Schema(description = "该项自付金额")
        private BigDecimal selfAmount;
    }
}
