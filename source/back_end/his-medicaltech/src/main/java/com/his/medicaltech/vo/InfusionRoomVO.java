package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门诊输液室出参集合（M10）。
 */
public class InfusionRoomVO {

    /** 座位（含占用输液单摘要） */
    @Data
    public static class Seat {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String seatNo;
        private String area;
        /** 1-空闲 2-占用 3-停用 */
        private Integer seatStatus;
        /** 占用输液单ID（空闲为空） */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long infusionId;
        /** 占用患者姓名（空闲为空） */
        private String patientName;
        /** 占用输液状态（空闲为空） */
        private Integer infusionStatus;
        /** 备注 */
        private String remark;
    }

    /** 输液单 */
    @Data
    public static class Infusion {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String infusionNo;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long treatmentRecordId;
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
        private String drugSummary;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long seatId;
        private String seatNo;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long skinTestId;
        /** 皮试判读结果（无皮试为空） */
        private Integer skinTestResult;
        /** 皮试药物（无皮试为空） */
        private String skinTestDrug;
        /** 1-待皮试 2-待输注 3-输液中 4-已完成 5-已取消 */
        private Integer status;
        /** 开始时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        private Integer dripRate;
        /** 结束时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
        private Integer adverseFlag;
        private String adverseDesc;
        private String nurseName;
        /** 取消原因 */
        private String cancelReason;
        /** 创建时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        /** 备注 */
        private String remark;
    }

    /** 巡视记录 */
    @Data
    public static class Round {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        @JsonSerialize(using = ToStringSerializer.class)
        private Long infusionId;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime roundTime;
        private Integer dripRate;
        private Integer remainingVolume;
        private String nurseName;
        /** 备注 */
        private String remark;
    }

    /** 今日看板：座位图 + 队列统计 */
    @Data
    public static class Board {
        private List<Seat> seats;
        /** 今日各状态计数（含取消/完成） */
        private Integer pendingTest;
        private Integer waiting;
        private Integer infusing;
        private Integer finished;
        private Integer cancelled;
    }
}
