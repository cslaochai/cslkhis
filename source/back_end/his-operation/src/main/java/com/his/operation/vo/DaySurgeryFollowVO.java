package com.his.operation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 日间手术随访流水 VO（追加式台账）。
 */
@Data
public class DaySurgeryFollowVO implements Serializable {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 登记单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /** 随访方式（1-电话 2-门诊 3-上门 4-线上） */
    private Integer followType;

    /** 随访结果（1-无异常 2-有异常已处置 3-有异常再就诊 4-失联） */
    private Integer result;

    /** 随访内容 */
    private String content;

    /** 随访人（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /** 随访人姓名 */
    private String operator;

    /** 随访时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followTime;
}
