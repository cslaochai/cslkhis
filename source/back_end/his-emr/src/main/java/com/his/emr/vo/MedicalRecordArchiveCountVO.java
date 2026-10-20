package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

/** 病案归档状态计数：病案归档列表各归档状态的病案数出参。 */
@Data
public class MedicalRecordArchiveCountVO implements Serializable {

    /** 归档病案总数（当前科室数据范围内，含状态为空的行） */
    private Integer total;

    /** 待归档数（归档状态 1-待归档） */
    private Integer pending;

    /** 已归档数（归档状态 2-已归档） */
    private Integer archived;

    /** 已封存数（归档状态 3-已封存） */
    private Integer sealed;
}
