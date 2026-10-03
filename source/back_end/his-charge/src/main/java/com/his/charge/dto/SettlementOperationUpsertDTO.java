package com.his.charge.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 结算清单手术操作明细维护入参
 */
@Data
public class SettlementOperationUpsertDTO {

    /**
     * 主键ID，新增时为空
     */
    private Long id;

    /**
     * 序号
     */
    private Integer seqNo;

    /**
     * 手术操作编码（ICD-9-CM-3）
     */
    private String operCode;

    /**
     * 手术操作名称
     */
    private String operName;

    /**
     * 手术操作日期
     */
    private LocalDate operDate;

    /**
     * 手术级别：1-4
     */
    private Integer operLevel;

    /**
     * 是否主要手术操作（0-否 1-是）
     */
    private Integer isMain;

    /**
     * 备注
     */
    private String remark;
}
