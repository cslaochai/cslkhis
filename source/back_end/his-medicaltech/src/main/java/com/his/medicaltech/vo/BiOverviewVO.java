package com.his.medicaltech.vo;

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

    /** 在院人数（未出院的入院记录） */
    private Long inHospitalCount;

    /** 今日出院人数 */
    private Long todayDischargeCount;

    /** 今日收入（收费明细净额=收入-退款抵扣） */
    private BigDecimal todayRevenue;

    /** 今日药费收入 */
    private BigDecimal todayDrugRevenue;

    /** 今日药占比（0~1） */
    private BigDecimal drugRatio;

    /** 床位占用率（占用/(总-维修)） */
    private BigDecimal bedOccupancy;

    /** 总床位数 */
    private Long bedTotal;

    /** 占用床位数 */
    private Long bedOccupied;

    /** 近 7 日门诊量趋势 */
    private List<TrendPoint> appointmentTrend;

    /** 近 7 日收入趋势（元） */
    private List<TrendPoint> revenueTrend;

    /** 收入 TOP5 科室（近 30 日） */
    private List<DeptRevenue> deptTop;

    @Data
    public static class TrendPoint {
        private String date;
        private Long count;
        private BigDecimal amount;
    }

    @Data
    public static class DeptRevenue {
        /** 科室名称 */
        private String deptName;
        private BigDecimal amount;
    }
}
