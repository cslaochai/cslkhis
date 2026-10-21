package com.his.clinical.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 慢病档案分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChronicPageQueryDTO extends PageParam implements Serializable {
    /** 患者ID */
    private Long patientId;
    /** 患者姓名 */
    private String patientName;
    private String recordNo;
    /**
     * 慢病名称或 ICD-10 编码模糊匹配
     */
    private String diseaseKeyword;
    /**
     * 认定状态（0-待认定 1-已认定 2-已取消）
     */
    private Integer confirmStatus;
}