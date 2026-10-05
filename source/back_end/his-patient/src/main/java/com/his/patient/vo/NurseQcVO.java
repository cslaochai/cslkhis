package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 护理质控出参（外层空壳 + 内层静态类，同 {@code NurseScheduleVO}）。
 *
 * <p><b>所有 Long 主键/外键一律字符串序列化</b>：雪花 19 位超出 JS {@code Number.MAX_SAFE_INTEGER}，
 * 裸数字回前端会丢精度，拿丢过的值去改单就是「检查单不存在」。
 *
 * <p>日期时间在 SQL 侧用 {@code DATE_FORMAT} 别名输出字符串（AGENTS §3），月份本来就是 CHAR(7)，
 * 所以本类没有任何 {@code LocalDate}/{@code LocalDateTime} 字段，序列化行为完全可预测。
 *
 * <p>状态/类别/达标/上报这些码值的中文一律由服务端翻译好放进 {@code xxxText} 字段：
 * 台账与检查表都在护理部通报里被截图，前端再抄一份码表迟早和字典漂移。
 */
public class NurseQcVO {

    /**
     * 病区（页面顶部筛选与看板抬头）
     */
    @Data
    public static class Ward implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        private String wardCode;
        /**
         * 病区名称（快照）
         */
        private String wardName;
        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
        /**
         * 开放床位（判断「这个病区的合格率是不是只有 3 张床撑出来的」）
         */
        private Integer totalBeds;
        private Integer occupiedBeds;
    }

    /**
     * 检查人候选（病区所属科室的在职人员，护士长/护理部质控组）
     */
    @Data
    public static class Inspector implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;
        private String empCode;
        private String empName;
        private String title;
        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
    }

    /**
     * 检查项标准（目录行，也是新检查单表单的行来源）
     */
    @Data
    public static class ItemDef implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long itemId;
        private String itemCode;
        /**
         * 项目名称
         */
        private String itemName;
        /**
         * 类别
         */
        private Integer category;
        private String categoryName;
        /**
         * 计入的台账指标编码，null=只进检查表不出指标
         */
        private String indicatorCode;
        private String standard;
        private BigDecimal fullScore;
        private BigDecimal targetRate;
        private Integer keyFlag;
        private Integer sortOrder;
    }

    /**
     * 检查单主表行（列表与详情共用）
     */
    @Data
    public static class CheckRow implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String checkNo;
        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        /**
         * 病区名称（快照）
         */
        private String wardName;
        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
        private String checkMonth;
        private String checkDate;
        /**
         * 类别
         */
        private Integer category;
        private String categoryName;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long inspectorId;
        private String inspectorName;
        private Integer sampleCount;
        private Integer qualifiedCount;
        private BigDecimal qualifiedRate;
        private BigDecimal fullScore;
        private BigDecimal totalScore;
        private BigDecimal scoreRate;
        private Integer status;
        /**
         * 状态文本
         */
        private String statusText;
        /**
         * 小结
         */
        private String summary;
        /**
         * 创建时间
         */
        private String createTime;
    }

    /**
     * 检查单明细行（逐项抽查/合格/得分 + PDCA 三段文本）
     */
    @Data
    public static class CheckItemRow implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long checkId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long itemId;
        private String itemCode;
        /**
         * 项目名称
         */
        private String itemName;
        /**
         * 类别
         */
        private Integer category;
        private Integer checkedNum;
        private Integer qualifiedNum;
        /**
         * 单项合格率（现算，不落列：由抽查/合格两数唯一决定）
         */
        private BigDecimal qualifiedRate;
        private BigDecimal fullScore;
        private BigDecimal score;
        private String problem;
        private String causeAnalysis;
        private String rectifyMeasure;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 检查单详情：主表 + 已录明细 + 本类别可选目录（catalog 用来发现「这一轮还有项没查」）
     */
    @Data
    public static class CheckDetail implements Serializable {
        private static final long serialVersionUID = 1L;
        private CheckRow check;
        /**
         * 明细项集合
         */
        private List<CheckItemRow> items;
        private List<ItemDef> catalog;
        /**
         * 目录里还没录入的项数（>0 时页面提示「本轮漏查 N 项」）
         */
        private Integer missingItemCount;
    }

    /**
     * 保存结果：回写服务端算好的六个汇总数，前端不再自己加一遍
     */
    @Data
    public static class SaveResult implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String checkNo;
        private Integer itemCount;
        private Integer sampleCount;
        private Integer qualifiedCount;
        private BigDecimal qualifiedRate;
        private BigDecimal fullScore;
        private BigDecimal totalScore;
        private BigDecimal scoreRate;
        private Integer status;
        /**
         * 状态文本
         */
        private String statusText;
        /**
         * 消息内容
         */
        private String message;
    }

    /**
     * 月度 KPI（按指标聚合：分子/分母/指标值 + 未达标病区数）
     */
    @Data
    public static class Kpi implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 统计月份（yyyy-MM）
         */
        private String statMonth;
        private String indicatorCode;
        private String indicatorName;
        /**
         * 单位
         */
        private String unit;
        private BigDecimal numerator;
        /**
         * 抽查例数（合格率类）或实际占用床日数（千床日类）
         */
        private BigDecimal denominator;
        private BigDecimal rateValue;
        /**
         * 目标值，null=千床日类不硬设目标（页面显示「—」而不是「未达标」）
         */
        private BigDecimal targetValue;
        private Integer sourceType;
        /**
         * 整体达标（1/0/NULL=无目标或无数据），页面卡片据此上色
         */
        private Integer reachedFlag;
        /**
         * 参与聚合的病区数
         */
        private Integer wardCount;
        private Integer notReachedCount;
        private Integer reportedCount;
        private Integer unreportedCount;
        /**
         * 指标方向：true=越高越好（合格率），false=越低越好（发生率）
         */
        private Boolean higherIsBetter;
        /**
         * 达标文案（达标/未达标/无目标），由服务端按 target 与 rate 现算
         */
        private String reachedText;
    }

    /**
     * 台账行（分页明细与病区对比共用）
     */
    @Data
    public static class LedgerRow implements Serializable {
        private static final long serialVersionUID = 1L;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /**
         * 病区ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;
        /**
         * 病区名称（快照）
         */
        private String wardName;
        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
        /**
         * 统计月份（yyyy-MM）
         */
        private String statMonth;
        private String indicatorCode;
        private String indicatorName;
        /**
         * 单位
         */
        private String unit;
        private BigDecimal numerator;
        private BigDecimal denominator;
        private BigDecimal rateValue;
        private BigDecimal targetValue;
        private Integer reachedFlag;
        private String reachedText;
        private Integer sourceType;
        private Integer reportStatus;
        private String reportStatusText;
        private String calcTime;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 重算结果
     */
    @Data
    public static class RecalcResult implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 统计月份（yyyy-MM）
         */
        private String statMonth;
        private Integer wardCount;
        /**
         * 本次写入/覆盖的台账行数
         */
        private Integer calculatedCount;
        /**
         * 因「已上报」被跳过的行数（数字要改先退回未上报）
         */
        private Integer skippedReportedCount;
        /**
         * 整月扫描时被跳过的「本月无住院事实且无检查单」病区数（单病区重算恒为 0）
         */
        private Integer skippedEmptyWardCount;
        /**
         * 本次重算涉及的床日合计（分母来源，用来核对我说「床日怎么是这个数」）
         */
        private Integer totalBedDays;
        /**
         * 消息内容
         */
        private String message;
    }

    /**
     * 上报/退回结果
     */
    @Data
    public static class ReportResult implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 统计月份（yyyy-MM）
         */
        private String statMonth;
        private Integer affectedCount;
        private Integer reportStatus;
        private String reportStatusText;
        /**
         * 消息内容
         */
        private String message;
    }
}
