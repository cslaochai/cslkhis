package com.his.report.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报表统计总览 VO（一次返回四个页签全部聚合结果，页面不再二次拼装）。
 *
 * <p>口径速查：
 * 门诊量 = visit_date 落在区间内且 regist_status&lt;&gt;5（5-已退号）的挂号记录；
 * 收入净额 = 旧收费明细按 is_refund 正负抵扣（与 BI 驾驶舱同源）；
 * 出院队列 = discharge_time 非空且落区间、排除 7-医嘱取消入院（不以 admit_status 判出院，dev 有脏行）。
 */
@Data
public class StatsOverviewVO {

    /** 开始日期 */
    private String startDate;
    /** 结束日期 */
    private String endDate;

    // 门诊

    /** 门诊量（regist_status<>5） */
    private Long opVisitTotal;
    /** 挂号总数（含退号） */
    private Long opRegistTotal;
    /** 退号数 */
    private Long opRefundCount;
    /** 初诊数（visit_type=1） */
    private Long opFirstVisitCount;
    /** 复诊数（visit_type=2） */
    private Long opRevisitCount;
    /** 医保结算门诊数（settlement_type<>1） */
    private Long opInsuranceCount;
    /** 门诊日趋势：d=MM-dd, n=门诊量, cancels=退号数 */
    private List<OpTrendRow> opTrend;
    /** 科室门诊量 TOP10 */
    private List<NameCountRow> opDeptTop;
    /** 挂号类型构成（regist_type） */
    private List<CodeCountRow> opTypeDist;
    /** 挂号来源构成（regist_source） */
    private List<CodeCountRow> opSourceDist;
    /** 结算方式构成（settlement_type） */
    private List<CodeCountRow> opSettleDist;
    /** 年龄段构成（按年龄字典序输出，图例顺序前端定） */
    private List<NameCountRow> opAgeDist;

    // 住院

    /** 期间入院人次 */
    private Long ipAdmitCount;
    /** 期间出院人次（出院队列口径） */
    private Long ipDischargeCount;
    /** 出院床日合计 */
    private Long ipBedDays;
    /** 出院者平均住院日（床日/出院人次，1位小数） */
    private BigDecimal ipAvgLosDays;
    /** 截至区间末在院人数（admit_time<=end 且未出院或出院在end后） */
    private Long ipInCur;
    /** 编制床位数（不含维修） */
    private Long bedTotal;
    /** 已占用床位 */
    private Long bedOccupied;
    /** 床位使用率 %（1位小数） */
    private BigDecimal bedUseRate;
    /** 入院/出院日趋势 */
    private List<IpTrendRow> ipTrend;
    /** 出院队列按现科室分布（含例均住院日） */
    private List<IpDeptRow> ipDeptDist;
    /** 住院结算汇总（有效结算单，排除3-已作废） */
    private IpSettleSummary ipSettle;
    /** 出院队列险种构成 */
    private List<NameCountRow> ipInsuranceDist;

    // 收入

    /** 收入净额 */
    private BigDecimal revTotal;
    /** 药品收入净额（item_type 2/3/4） */
    private BigDecimal revDrug;
    /** 药占比 %（1位小数） */
    private BigDecimal drugRatio;
    /** 材料收入净额（source_id 关联处方且 item_type=8） */
    private BigDecimal revMaterial;
    /** 门诊收入净额（source_id 非空，含挂号费） */
    private BigDecimal revOutpatient;
    /** 住院收入净额（source_id 为空，即住院医嘱记账） */
    private BigDecimal revInpatient;
    /** 收入日趋势 */
    private List<DateAmountRow> revTrend;
    /** 收入类别构成（item_type 1~7） */
    private List<NameAmountRow> revTypeDist;
    /** 科室收入 TOP10 */
    private List<NameAmountRow> revDeptTop;
    /** 支付方式构成（已收/已部分退费单） */
    private List<CodeCountAmountRow> revPayDist;
    /** 期间退费额与笔数（旧收费单 is_refund=1） */
    private BigDecimal revRefundAmount;
    private Long revRefundCount;

    // 药事

    /** 已提交处方数（prescription_status>=2 且<>5，按 visit_date） */
    private Long phPrescCount;
    /** 门诊/急诊/住院处方数（按开单主体 prescription_source，不按类型1/2/3分支） */
    private Long phPrescOutpatient;
    private Long phPrescEmergency;
    private Long phPrescInpatient;
    /** 处方类型构成（西药/中成药/中药饮片） */
    private List<CodeCountAmountRow> phPrescTypeDist;
    /** 期间处方被药师退回次数合计 */
    private Long phAuditReturnCount;
    /** 按药品维度处方金额 TOP10（处方明细，覆盖门诊+住院医嘱药） */
    private List<DrugRankRow> phDrugTop;
    /** 发药金额日趋势（dispensing_status=2） */
    private List<DateAmountRow> phDispTrend;
    /** 退药笔数（dispensing_status=3） */
    private Long phReturnCount;
    /** 退药金额 */
    private BigDecimal phReturnAmount;

    // 行类型

    @Data
    public static class OpTrendRow {
        private String d;
        private Long n;
        private Long cancels;
    }

    @Data
    public static class NameCountRow {
        /** 名称 */
        private String name;
        private Long cnt;
    }

    @Data
    public static class CodeCountRow {
        private Integer code;
        private Long cnt;
    }

    @Data
    public static class IpTrendRow {
        private String d;
        private Long admits;
        private Long discharges;
    }

    @Data
    public static class IpDeptRow {
        /** 科室名称 */
        private String deptName;
        private Long admits;
        private Long discharges;
        private BigDecimal avgLos;
    }

    @Data
    public static class IpSettleSummary {
        private Long settleCount;
        /** 合计金额 */
        private BigDecimal totalAmount;
        private BigDecimal insuranceAmount;
        private BigDecimal patientPayAmount;
        private BigDecimal arrearsAmount;
    }

    @Data
    public static class DateAmountRow {
        private String d;
        private BigDecimal amt;
    }

    @Data
    public static class NameAmountRow {
        /** 名称 */
        private String name;
        private BigDecimal amt;
    }

    @Data
    public static class CodeCountAmountRow {
        private Integer code;
        private Long cnt;
        private BigDecimal amt;
    }

    @Data
    public static class BedStat {
        private Long occupied;
        /** 总条数 */
        private Long total;
    }

    @Data
    public static class ReturnStat {
        private Long cnt;
        private BigDecimal amt;
    }

    @Data
    public static class DrugRankRow {
        /** 药品名称 */
        private String drugName;
        private String specification;
        /** 单位 */
        private String unit;
        private BigDecimal qty;
        private BigDecimal amt;
    }
}
