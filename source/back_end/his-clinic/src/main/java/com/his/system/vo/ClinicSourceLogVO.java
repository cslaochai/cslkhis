package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门诊排班变更留痕出参。
 */
@Data
public class ClinicSourceLogVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleId;

    /**
     * 排班日期（按留痕时关联的排班行补）
     */
    private String scheduleDate;

    /**
     * 科室名
     */
    private String deptName;

    /**
     * 医生名
     */
    private String doctorName;

    private Integer actionType;

    private String actionTypeText;

    private Integer amount;

    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;

    /**
     * 操作人
     */
    private String createBy;
}
