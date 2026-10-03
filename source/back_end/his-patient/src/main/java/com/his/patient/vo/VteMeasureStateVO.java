package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/** 某条措施的当前状态（名单行内嵌，未登记的 measureCode 不出现） */
@Data
public class VteMeasureStateVO {

    /** 所属住院（批量查询拼装用，名单行内不暴露给前端展示） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 措施码 */
    private String measureCode;

    /** 措施名称 */
    private String measureName;

    /** 措施类别（1-基础预防 2-物理预防 3-药物预防） */
    private Integer measureType;

    private String measureTypeText;

    /** 落实状态（0-待落实 1-已落实 2-禁忌未用 3-患者拒绝） */
    private Integer executeStatus;

    private String executeStatusText;

    /** 执行人姓名 */
    private String executorName;

    /** 落实时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;

    /** 原因 */
    private String reason;
}
