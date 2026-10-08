package com.his.appoint.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班下拉出参：挂号/复诊页选号源时真正要比对的列。
 *
 * <p>字段口径以页面实际读取为准，不跟 {@link ScheduleDetailVO}（列表/今日排班要带的
 * 科室、岗位、班次名、号源总数已用、诊查费、诊室 ID、停诊状态等在这里都用不上）。
 */
@Data
@Schema(name = "ScheduleSelectListVO", description = "排班下拉出参")
public class ScheduleSelectListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    private String doctorName;

    private LocalDate scheduleDate;

    /**
     * 排班类型（号源类别）
     */
    private Integer scheduleType;

    private String startTime;

    private String endTime;

    /**
     * 诊室名称
     */
    private String roomName;

    /**
     * 挂号费
     */
    private BigDecimal registFee;

    /**
     * 剩余号源
     */
    private Integer availableSource;
}
