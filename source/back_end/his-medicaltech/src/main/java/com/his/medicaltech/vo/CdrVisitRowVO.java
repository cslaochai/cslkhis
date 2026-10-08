package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CDR 门诊就诊次行（CdrMapper#selectVisits 一行）。
 */
@Data
public class CdrVisitRowVO implements Serializable {

    /**
     * 就诊次ID（字符串，避免前端丢精度）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitId;

    /**
     * 就诊次编号
     */
    private String visitNo;

    /**
     * 数据归属档案ID（EMPI 归并后可能不是主档）
     */
    private String ownerPid;

    /**
     * 就诊开始时间
     */
    private LocalDateTime startTime;

    /**
     * 就诊结束时间（未结束时为 null）
     */
    private LocalDateTime endTime;

    /**
     * 就诊状态（0-已取消 1-进行中 2-已完成）
     */
    private Integer visitStatus;

    /**
     * 本次就诊总费用
     */
    private BigDecimal totalAmount;

    /**
     * 本次就诊收录的挂号ID清单（逗号分隔）
     */
    private String registIds;
}