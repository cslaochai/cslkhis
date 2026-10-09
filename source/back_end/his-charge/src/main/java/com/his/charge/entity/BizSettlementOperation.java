package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 结算清单手术操作明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_settlement_operation")
public class BizSettlementOperation extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



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
     * 依据核对结果（1-命中 2-通过 3-不适用）
     */
    private Integer evidenceStatus;

    /**
     * 依据核对说明
     */
    private String evidenceNote;
}
