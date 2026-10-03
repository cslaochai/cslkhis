package com.his.emr.dto;

import lombok.Data;

/**
 * 慢病档案分页查询入参
 */
@Data
public class ChronicQueryPageDTO {
    private Long patientId;
    private String patientName;
    private String recordNo;
    /** 慢病名称或 ICD-10 编码模糊匹配 */
    private String diseaseKeyword;
    private Integer confirmStatus;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
