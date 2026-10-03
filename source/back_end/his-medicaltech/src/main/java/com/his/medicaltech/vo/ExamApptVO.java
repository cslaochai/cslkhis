package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 检查预约中心出参
 */
public class ExamApptVO {

    // 设备

    @Data
    public static class DeviceVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 设备编码 */
        private String deviceCode;
        /** 设备名称 */
        private String deviceName;
        /** 设备类型 */
        private Integer deviceType;
        private String deviceTypeText;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long equipmentId;
        private String equipmentName;
        /** 科室ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;
        /** 科室名称 */
        private String deptName;
        private String roomName;
        private String amStart;
        private String amEnd;
        private String pmStart;
        private String pmEnd;
        private Integer slotMinutes;
        private Integer parallelCount;
        private Integer aheadDays;
        private Integer maxSlotMinutes;
        private Integer status;
        /** 状态文本 */
        private String statusText;
        private String openRangeText;
        /** 按当前开放时间可切出的格子数（当日号源上限，不含并行数） */
        private Integer dailySlots;
        private Integer itemCount;
        /** 备注 */
        private String remark;
        /** 创建时间 */
        private LocalDateTime createTime;
        /** 保存后的口径提醒（如开放时间变更导致历史号源不一致），无提醒时为空 */
        private String warning;
        /** 仅 getDetailById 返回 */
        private List<DeviceItemVO> itemList;
    }

    /** 设备台账候选下拉出参（医疗设备台账只读挂接：G22 域的档案，本模块不建不改） */
    @Data
    public static class EquipmentSelectListVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String equipmentCode;
        private String equipmentName;
        /** 类别 */
        private Integer category;
        private Integer status;
    }

    /** 设备档位下拉出参 */
    @Data
    public static class DeviceSelectListVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 设备编码 */
        private String deviceCode;
        /** 设备名称 */
        private String deviceName;
        /** 设备类型 */
        private Integer deviceType;
        private String deviceTypeText;
        /** 科室名称 */
        private String deptName;
        private String roomName;
        private Integer status;
        private Integer slotMinutes;
        private Integer parallelCount;
    }

    @Data
    public static class DeviceItemVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        /** 设备ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deviceId;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long itemId;
        private String itemCode;
        /** 项目名称 */
        private String itemName;
        private Integer examMinutes;
        /** 项目字典自身时长（对照用：设备侧留空时按它算） */
        private Integer itemDictMinutes;
    }

    /** 检查项目候选下拉出参，字段口径同 {@link DeviceItemVO} */
    @Data
    public static class ItemSelectListVO extends DeviceItemVO {
    }

    // 号源

    @Data
    public static class SlotCellVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long slotId;
        private Integer seq;
        /** 开始时间 */
        private String startTime;
        /** 结束时间 */
        private String endTime;
        private Integer totalSource;
        private Integer usedSource;
        private Integer availableSource;
        private Integer status;
        /** 占用该格的预约单号（设备冲突时要能指认"是谁占了"，只报"满了"没法处理） */
        private List<String> occupyApptNos;
        private List<String> occupyPatientNames;
        /** 该格是否已过期（当天且结束时刻早于现在） */
        private Boolean past;
    }

    @Data
    public static class SlotBoardVO {
        /** 设备ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deviceId;
        /** 设备编码 */
        private String deviceCode;
        /** 设备名称 */
        private String deviceName;
        /** 科室名称 */
        private String deptName;
        private String roomName;
        private Integer deviceStatus;
        private String deviceStatusText;
        private LocalDate slotDate;
        private Integer slotMinutes;
        private Integer parallelCount;
        private Integer totalSlots;
        private Integer freeSlots;
        private Integer usedSlots;
        private Integer lockedSlots;
        private List<SlotCellVO> slots;
    }

    @Data
    public static class SlotEnsureVO {
        private Integer created;
        private Integer existing;
        private Integer totalSlots;
        /** 消息内容 */
        private String message;
    }

    @Data
    public static class SlotDriftVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long slotId;
        /** 设备ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deviceId;
        /** 设备名称 */
        private String deviceName;
        private LocalDate slotDate;
        /** 开始时间 */
        private String startTime;
        private Integer usedBefore;
        private Integer usedActual;
    }

    @Data
    public static class SlotRecalcVO {
        private Integer checked;
        private Integer drifted;
        private List<SlotDriftVO> details;
        /** 消息内容 */
        private String message;
    }

    // 待预约申请

    @Data
    public static class ApplyVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long applyId;
        private String applyNo;
        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /** 患者编号 */
        private String patientNo;
        /** 患者姓名 */
        private String patientName;
        /** 性别（1-男 2-女 9-未知） */
        private Integer gender;
        /** 年龄 */
        private Integer age;
        /** 就诊日期 */
        private LocalDate visitDate;
        private String applyDeptName;
        /** 医生姓名 */
        private String doctorName;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long itemId;
        private String itemCode;
        /** 项目名称 */
        private String itemName;
        private String bodyPart;
        private Integer examMinutes;
        /** 单价 */
        private BigDecimal price;
        private Integer isEmergency;
        private Integer applyStatus;
        private String applyStatusText;
        /** 该项目已配置到哪台设备（0 = 无设备可承接，预约不了，要先去设备页配置） */
        private Integer deviceCount;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long activeApptId;
        private String activeApptNo;
        private LocalDate activeExamDate;
        private String activeTimeRange;
    }

    // 预约单

    @Data
    public static class ApptVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String apptNo;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long applyId;
        private String applyNo;
        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;
        /** 患者编号 */
        private String patientNo;
        /** 患者姓名 */
        private String patientName;
        /** 性别（1-男 2-女 9-未知） */
        private Integer gender;
        /** 年龄 */
        private Integer age;
        private String applyDeptName;
        /** 医生姓名 */
        private String doctorName;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long itemId;
        private String itemCode;
        /** 项目名称 */
        private String itemName;
        private String bodyPart;
        private Integer examMinutes;
        /** 设备ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deviceId;
        /** 设备编码 */
        private String deviceCode;
        /** 设备名称 */
        private String deviceName;
        private String examDeptName;
        private String roomName;
        private LocalDate examDate;
        /** 开始时间 */
        private String startTime;
        /** 结束时间 */
        private String endTime;
        private String timeRange;
        private Integer isEmergency;
        private Integer status;
        /** 状态文本 */
        private String statusText;
        private Integer activeFlag;
        private String bookBy;
        private LocalDateTime bookTime;
        private LocalDateTime arriveTime;
        private LocalDateTime finishTime;
        /** 取消时间 */
        private LocalDateTime cancelTime;
        /** 取消原因 */
        private String cancelReason;
        private LocalDateTime noshowTime;
        /** 备注 */
        private String remark;
        /** 创建时间 */
        private LocalDateTime createTime;
    }

    @Data
    public static class ApptDetailVO extends ApptVO {
        private String applyNoSnapshot;
        private String prevApplyStatusText;
    }

    /** 台账筛选口径下的状态分布（后端 group by） */
    @Data
    public static class StatusCountVO {
        private Integer status;
        /** 状态文本 */
        private String statusText;
        private Long count;
    }

    @Data
    public static class StatsVO {
        /** 今日各状态预约数（后端 group by，前端不得拿当前页 list 自己数） */
        private long todayBooked;
        private long todayArrived;
        private long todayFinished;
        private long todayCancelled;
        private long todayNoShow;
        /** 待预约申请数（apply_status 已缴费/已预约且无在办预约） */
        private long pendingApplies;
        /** 全院在办预约总数（不限日期） */
        private long activeTotal;
        private long deviceOpen;
        private long devicePaused;
    }

    @Data
    public static class SlotOptionVO {
        /** 设备ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deviceId;
        /** 设备编码 */
        private String deviceCode;
        /** 设备名称 */
        private String deviceName;
        private String deviceTypeText;
        private String roomName;
        private LocalDate examDate;
        /** 开始时间 */
        private String startTime;
        /** 结束时间 */
        private String endTime;
        private Integer availableSource;
    }

    @Data
    public static class RecommendVO {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long applyId;
        /** 项目名称 */
        private String itemName;
        private Integer examMinutes;
        /** 消息内容 */
        private String message;
        private List<SlotOptionVO> options;
    }
}
