package com.his.charge.dto;

import com.his.common.validation.AllowedValues;
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
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "手术级别取值不合法")
    private Integer operLevel;

    /**
     * 是否主要手术操作（0-否 1-是）
     */
    @AllowedValues(values = {"0", "1"}, message = "是否主要手术操作取值不合法")
    private Integer isMain;

    /**
     * 备注
     */
    private String remark;
}
