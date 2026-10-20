package com.his.system.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 全院总值班月度工时统计结果：一人一行，按实际值班人（换班后按顶替人）汇总。
 */
@Data
public class DutyWorkloadVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计月份 */
    private String month;
    /** 值班点位ID（空=全部） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long postId;
    /** 值班点位名称（postId 为空时为空） */
    private String postName;
    /** 参与排班人数 */
    private Integer dutyCount;
    /** 总班次数 */
    private Integer totalDuties;
    /** 总值班时长（小时） */
    private BigDecimal totalHours;
    /** 明细行（按工号排序） */
    private List<Row> rows;

    /**
     * 单个值班人统计行
     */
    @Data
    public static class Row implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 值班人ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;
        /** 工号 */
        private String empCode;
        /** 姓名 */
        private String employeeName;
        /** 原属科室 */
        private String deptName;
        /** 白班次数 */
        private Integer dayCount;
        /** 夜班次数 */
        private Integer nightCount;
        /** 主班次数 */
        private Integer primaryCount;
        /** 副班次数 */
        private Integer secondaryCount;
        /** 总时长（分钟） */
        private Integer totalMinutes;
        /** 总时长（小时，保留一位） */
        private BigDecimal totalHours;
    }
}
