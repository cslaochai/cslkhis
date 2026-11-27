package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * BI 驾驶舱总览 VO（一次接口全量返回，页面不做二次拼装）。
 */
@Data
public class BiOverviewVO {

    /** 今日挂号人次 */
    private Long todayAppointments;

    /** 今日入院人数（admit_time 当日） */
    private Long todayAdmissions;

    /** 在院人数（未出院的入院记录） */
    private Long inHospitalCount;

    /** 今日出院人数 */
    private Long todayDischargeCount;

    /** 今日主手术台次（is_main=1，operation_date 当日） */
    private Long todayOperations;

    /** 今日收入（收费明细净额=收入-退款抵扣） */
    private BigDecimal todayRevenue;

    /** 今日药占比（0~1） */
    private BigDecimal drugRatio;

    /** 床位占用率（占用/(总-维修)） */
    private BigDecimal bedOccupancy;

    /** 总床位数 */
    private Long bedTotal;

    /** 占用床位数 */
    private Long bedOccupied;

    /** 空床数（总床-占用-维修，不为负） */
    private Long emptyBeds;

    /** 待入院排队（待收治住院证，order_status=1） */
    private Long pendingAdmissionCount;

    // 昨日对照（环比用；存量指标在院人数不挂环比）

    /** 昨日挂号人次 */
    private Long yesterdayAppointments;

    /** 昨日入院人数 */
    private Long yesterdayAdmissions;

    /** 昨日出院人数 */
    private Long yesterdayDischargeCount;

    /** 昨日主手术台次 */
    private Long yesterdayOperations;

    /** 昨日收入净额 */
    private BigDecimal yesterdayRevenue;

    /** 昨日药占比（0~1） */
    private BigDecimal yesterdayDrugRatio;

    /** 近 7 日门诊量趋势 */
    private List<TrendPoint> appointmentTrend;

    /** 近 7 日收入趋势（元） */
    private List<TrendPoint> revenueTrend;

    /** 收入 TOP5 科室（近 30 日） */
    private List<DeptRevenue> deptTop;

    /** 出院人次 TOP5 科室（近 30 日） */
    private List<DeptWorkload> deptWorkloadTop;

    /** 近 30 日收入结构（六类归并） */
    private List<RevenueStructure> revenueStructure;

    @Data
    public static class TrendPoint {
        private String date;
        private Long count;
        private BigDecimal amount;
        /** 当日门诊收入净额（encounter_type=1） */
        private BigDecimal outpatientAmount;
        /** 当日住院收入净额（encounter_type=2） */
        private BigDecimal inpatientAmount;
    }

    @Data
    public static class DeptRevenue {
        /** 科室 ID（未分配科室为 null），供下钻科室明细报表 */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /** 科室名称 */
        private String deptName;
        private BigDecimal amount;
    }

    @Data
    public static class DeptWorkload {
        /** 科室 ID（未分配科室为 null），供下钻科室明细报表 */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /** 科室名称 */
        private String deptName;
        /** 出院人数 */
        private Long count;
    }

    /**
     * 收入结构一类（药品为西药/中成药/中药饮片归并）。
     */
    @Data
    public static class RevenueStructure {
        /** 类别编码：registration-诊查 drug-药品 exam-检查 lab-检验 treatment-治疗 consumable-耗材 */
        private String code;
        /** 类别名称 */
        private String name;
        /** 近 30 日收入净额 */
        private BigDecimal amount;
    }
}
