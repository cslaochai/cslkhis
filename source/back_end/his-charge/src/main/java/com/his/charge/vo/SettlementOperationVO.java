package com.his.charge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 结算清单手术操作明细出参
 */
@Data
public class SettlementOperationVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 结算清单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settlementId;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
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
     * 依据核对结果：1-命中 2-通过 3-不适用
     */
    private Integer evidenceStatus;

    /**
     * 依据核对结果中文（三态）
     */
    private String evidenceStatusText;

    /**
     * 依据核对说明
     */
    private String evidenceNote;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
