package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 慢病档案分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChronicQueryPageDTO extends PageParam implements Serializable {
    private Long patientId;
    private String patientName;
    private String recordNo;
    /**
     * 慢病名称或 ICD-10 编码模糊匹配
     */
    private String diseaseKeyword;
    private Integer confirmStatus;
}