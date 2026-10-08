package com.his.operation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 手术室排台总表出参（P134.1）：一天 × 手术间的矩阵。
 */
@Data
public class OperationScheduleMatrixVO implements Serializable {

    /**
     * 总表日期 yyyy-MM-dd
     */
    private String date;

    /**
     * 手术间列（按 sortOrder、roomCode 升序，只含启用中的）
     */
    private List<RoomColumn> rooms;

    /**
     * 未登记手术间兜底桶（手术间名 → 该名下当天手术）
     */
    private List<RoomColumn> others;

    /**
     * 待排期申请（急诊在前，按申请时间升序）
     */
    private List<OperationApplyVO> unscheduled;

    /**
     * 当天已排台手术台次（含核对完成/已完成，不含待排期）
     */
    private Integer scheduledCount;

    /**
     * 当天急诊台次
     */
    private Integer emergencyCount;

    @Data
    public static class RoomColumn implements Serializable {
        /**
         * 手术间主数据ID（others 桶为 null）
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long roomId;

        /**
         * 手术间编码（others 桶为 null）
         */
        private String roomCode;

        /**
         * 手术间名称 —— 与手术申请单.operation_room 快照对齐的键
         */
        private String roomName;

        /**
         * 位置（others 桶为 null）
         */
        private String location;

        /**
         * 该列当天的手术（计划开始时间升序）
         */
        private List<OperationApplyVO> ops;
    }
}
