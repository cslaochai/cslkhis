package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 全院床位总览
 */
@Data
public class BedOverviewVO {

    /**
     * 小结
     */
    private Summary summary = new Summary();

    /**
     * 科室床位排行（按空闲升序：最紧张的排前面）
     */
    private List<DeptRow> deptRows;

    @Data
    public static class Summary {
        private long totalBeds;
        private long freeBeds;
        private long occupiedBeds;
        private long lockedBeds;
        private long repairBeds;
        /**
         * 可用床 = 总数 - 维修（含空闲与已占用）
         */
        private long usableBeds;
        /**
         * 床位使用率（%）= 占用 / 可用床
         */
        private BigDecimal usageRate;
        /**
         * 已被跨科借出/预留的床位数
         */
        private long lentOutBeds;
    }

    @Data
    public static class DeptRow {
        /**
         * 科室ID
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
        private long totalBeds;
        private long freeBeds;
        private long occupiedBeds;
        private long lockedBeds;
        /**
         * 本科室床位被借出（预留中）的床数
         */
        private long lentOutBeds;
    }
}
