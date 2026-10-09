package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DRG 并发症合并症(CC/MCC)目录（官方下发，按 version 区分 2.0/3.0）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_drg_ccmcc")
public class SysDrgCcmcc extends BaseEntity {

    /**
     * 诊断编码（ICD-10）
     */
    private String icdCode;

    /**
     * 级别（MCC-严重并发症合并症 CC-并发症合并症 NONE-无）
     */
    private String ccLevel;

    /**
     * 分组方案版本（2.0/3.0）
     */
    private String version;

    /**
     * 来源
     */
    private String source;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
