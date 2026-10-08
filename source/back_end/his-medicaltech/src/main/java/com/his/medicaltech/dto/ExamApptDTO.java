package com.his.medicaltech.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 检查预约中心入参（设备档位 / 号源 / 预约单）
 */
public class ExamApptDTO {

    // 设备

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DeviceQuery extends PageParam {
        /**
         * 关键字
         */
        private String keyword;
        /**
         * 设备类型
         */
        private Integer deviceType;
        /**
         * 科室ID
         */
        private Long deptId;
        private Integer status;
    }

    @Data
    public static class DeviceUpsert {
        private Long id;

        /**
         * 设备编码
         */
        @NotBlank(message = "设备编码不能为空")
        private String deviceCode;

        /**
         * 设备名称
         */
        @NotBlank(message = "设备名称不能为空")
        private String deviceName;

        /**
         * 设备类型
         */
        @NotNull(message = "设备类别不能为空")
        private Integer deviceType;

        /**
         * 设备台账ID（医疗设备台账的ID，可空 —— 台账没建档也允许先排号源）
         */
        private Long equipmentId;

        /**
         * 科室ID
         */
        private Long deptId;
        /**
         * 科室名称
         */
        private String deptName;
        private String roomName;

        @NotBlank(message = "上午开放开始时间不能为空")
        private String amStart;

        @NotBlank(message = "上午开放结束时间不能为空")
        private String amEnd;

        private String pmStart;
        private String pmEnd;

        private Integer slotMinutes;
        private Integer parallelCount;
        private Integer aheadDays;
        private Integer maxSlotMinutes;
        private Integer status;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 设备可开展项目：整单覆盖式保存（本次未提交的映射视为取消）
     */
    @Data
    public static class DeviceItemSave {
        /**
         * 设备ID
         */
        @NotNull(message = "设备ID不能为空")
        private Long deviceId;

        /**
         * 明细项集合
         */
        private List<ItemRef> items;
    }

    @Data
    public static class ItemRef {
        @NotNull(message = "检查项目ID不能为空")
        private Long itemId;

        /**
         * 设备侧时长覆盖（分钟），空=取项目字典时长
         */
        private Integer examMinutes;
    }

    // 号源

    @Data
    public static class SlotEnsure {
        /**
         * 设备ID
         */
        @NotNull(message = "设备ID不能为空")
        private Long deviceId;

        /**
         * 开始日期
         */
        @NotNull(message = "号源日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /**
         * 从 startDate 起连生成几天（含当天），默认 1，上限受设备 ahead_days 约束
         */
        private Integer days;
    }

    @Data
    public static class SlotQuery {
        /**
         * 设备ID
         */
        @NotNull(message = "设备ID不能为空")
        private Long deviceId;

        @NotNull(message = "号源日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate slotDate;
    }

    @Data
    public static class SlotToggle {
        @NotNull(message = "号源ID不能为空")
        private Long slotId;

        @NotNull(message = "状态不能为空")
        private Integer status;

        /**
         * 原因
         */
        private String reason;
    }

    @Data
    public static class SlotRecalc {
        /**
         * 设备ID
         */
        private Long deviceId;

        @NotNull(message = "起始日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateFrom;

        @NotNull(message = "截止日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate dateTo;
    }

    // 待预约申请 / 预约单

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ApplyQuery extends PageParam {
        /**
         * 关键字
         */
        private String keyword;
        /**
         * 患者ID
         */
        private Long patientId;
        private Integer isEmergency;

        /**
         * 开始日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /**
         * 结束日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ApptQuery extends PageParam {
        private String apptNo;
        /**
         * 关键字
         */
        private String keyword;
        /**
         * 患者ID
         */
        private Long patientId;
        /**
         * 设备ID
         */
        private Long deviceId;
        private Long examDeptId;
        private Integer status;
        /**
         * 设备类型
         */
        private Integer deviceType;

        /**
         * 开始日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /**
         * 结束日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;
    }

    @Data
    public static class Book {
        @NotNull(message = "检查申请单不能为空")
        private Long applyId;

        /**
         * 设备ID
         */
        @NotNull(message = "设备不能为空")
        private Long deviceId;

        @NotNull(message = "检查日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate examDate;

        /**
         * 开始时间
         */
        @NotBlank(message = "开始时段不能为空")
        private String startTime;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    public static class Reschedule {
        @NotNull(message = "预约单不能为空")
        private Long apptId;

        /**
         * 设备ID
         */
        @NotNull(message = "设备不能为空")
        private Long deviceId;

        @NotNull(message = "检查日期不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate examDate;

        /**
         * 开始时间
         */
        @NotBlank(message = "开始时段不能为空")
        private String startTime;

        /**
         * 原因
         */
        @NotBlank(message = "改约原因不能为空")
        private String reason;
    }

    @Data
    public static class Cancel {
        @NotNull(message = "预约单不能为空")
        private Long apptId;

        /**
         * 取消原因
         */
        @NotBlank(message = "取消原因不能为空")
        private String cancelReason;
    }

    @Data
    public static class ApptIdOnly {
        @NotNull(message = "预约单不能为空")
        private Long apptId;
    }

    /**
     * 可选时段推荐：给"这个时段满了，最近的空档在哪"一个答案
     */
    @Data
    public static class Recommend {
        @NotNull(message = "检查申请单不能为空")
        private Long applyId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate examDate;

        /**
         * 设备ID
         */
        private Long deviceId;

        /**
         * 只推荐这个时刻之后的时段（HH:mm，可空）
         */
        private String afterTime;
    }
}
