package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 待补建随访计划的出院记录（存活出院且尚无对应任务）。
 *
 * <p>{@code hasOperation} 决定随访类型：有手术排术后随访，否则排复诊提醒，
 * 在 SQL 里一次带出免得逐条回查手术表。
 */
@Data
public class DischargePendingFollowupVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long dischargeId;

    /**
     * 本次住院是否有手术（1-有 0-无）
     */
    private Integer hasOperation;
}