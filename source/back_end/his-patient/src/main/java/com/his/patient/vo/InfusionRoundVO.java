package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 输液巡视记录出参。
 */
@Data
public class InfusionRoundVO {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 执行行ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long execId;

    /** 巡视时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime roundTime;

    /** 滴速（滴/分） */
    private Integer dripRate;
    /** 余量（ml） */
    private Integer remainingVolume;

    /** 巡视护士ID（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roundNurseId;

    /** 巡视护士姓名 */
    private String roundNurseName;

    /** 备注 */
    private String remark;
}
