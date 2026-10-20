package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 队列状态行：结诊联动要读的接诊医生与要推进的队列状态。
 */
@Data
public class QueueStateRowVO {

    /**
     * 队列ID（排队记录主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 队列编号
     */
    private String queueNo;

    /**
     * 队列状态（见宿主枚举）
     */
    private Integer queueStatus;

    /**
     * 接诊医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 接诊结束时刻
     */
    private LocalDateTime endTime;
}
