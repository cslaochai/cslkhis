package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 待补建随访计划的出院记录（存活出院且尚无对应任务）。
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