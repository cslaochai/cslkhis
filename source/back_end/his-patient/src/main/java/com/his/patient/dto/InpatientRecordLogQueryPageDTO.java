package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 住院文书修改日志分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InpatientRecordLogQueryPageDTO extends PageParam implements Serializable {

    /**
     * 单据类型：1-住院病历文书 2-护理文书
     */
    private Integer docType;

    /**
     * 单据ID
     */
    private Long recordId;

    /**
     * 单据号
     */
    private String recordNo;

    /**
     * 入院ID（按住院查全部文书的修改轨迹时用；日志表本身不存 admission_id，
     * 由服务层先查出该入院的文书号集合再按号过滤）
     */
    private Long admissionId;
}
