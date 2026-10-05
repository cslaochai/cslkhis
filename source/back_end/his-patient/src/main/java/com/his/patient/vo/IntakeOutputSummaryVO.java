package com.his.patient.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 出入量小结出参：从护理文书（类型 1/3 的出入量字段）按日复算，**不是另存的统计表**——
 * 小结必须能从原始测量行推出来，否则原始行修改后小结就是死的。
 */
@Data
public class IntakeOutputSummaryVO {

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 患者姓名
     */
    private String patientName;

    private String beginDate;

    /**
     * 结束日期
     */
    private String endDate;

    private Integer recordCount;

    /**
     * 按日小结（日期升序）
     */
    private List<DayRow> days;

    /**
     * 合计
     */
    private DayRow totals;

    /**
     * 净平衡 = 总入量 - 总出量（正为正平衡，提示水钠潴留风险）
     */
    private BigDecimal netBalance;

    @Data
    public static class DayRow {
        /**
         * yyyy-MM-dd
         */
        private String date;
        /**
         * 入量（ml）
         */
        private Integer intake;
        /**
         * 出量（ml）
         */
        private Integer output;
        /**
         * 尿量（ml）
         */
        private Integer urine;
        /**
         * 大便（次/日，不参与 ml 汇总但单列展示）
         */
        private Integer stool;
        /**
         * 当日净平衡（入-出）
         */
        private Integer netBalance;
        /**
         * 测量点数
         */
        private Integer pointCount;
    }
}
