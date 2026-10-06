package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 结算清单手术操作明细
 *
 * <p>DRG 分组的另一半输入（诊断定 ADRG，手术决定外科组）。
 * 缺这张表，「高套手术操作」和「有手术收费却无手术编码」都查不出来 ——
 * 而后者是医保飞检最常见的扣分项。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_settlement_operation")
public class BizSettlementOperation extends BaseEntity {

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
