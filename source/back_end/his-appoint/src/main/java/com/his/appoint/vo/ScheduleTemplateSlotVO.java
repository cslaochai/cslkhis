package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 模板段级号源配置 VO（模板详情/列表回显用）；
 */
@Data
public class ScheduleTemplateSlotVO {

    /**
     * 段配置ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 段序（1起，按段开始时间升序）
     */
    private Integer seq;

    /**
     * 段开始（HH:mm）
     */
    private String startTime;

    /**
     * 段结束（HH:mm）
     */
    private String endTime;

    /**
     * 段号源总数
     */
    private Integer totalSource;

    /**
     * 段内线上预约预留（0=未划池）
     */
    private Integer appointmentSource;
}
