package com.his.iot.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配送任务单 VO。
 */
@Data
public class RobotDispatchOrderVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String orderNo;

    private String sourceType;
    private String sourceTypeLabel;
    private String sourceDeptName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long requesterId;
    private String requesterName;

    private String goodsType;
    private String goodsName;
    private String fromStation;
    private Integer toFloor;
    private String toStation;

    private Integer priority;
    private String priorityLabel;
    private Integer boxCount;
    private Double loadKg;

    private Integer status;
    private String statusLabel;

    private String robotNo;
    private String robotName;
    private String signer;

    private String dispatchBy;
    private LocalDateTime dispatchTime;
    private LocalDateTime completeTime;

    private Integer totalSec;
    private Integer elevatorSec;
    private Integer queueSec;
    private Integer rideSec;
    private Double odometerM;

    private String cancelReason;
    private String remark;
    private LocalDateTime createTime;
}
