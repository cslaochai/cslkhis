package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CSSD 器械包实体（86 号脚本新增）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_cssd_pack")
public class BizCssdPack extends BaseEntity {

    /**
     * 器械包条码
     */
    private String packNo;
    /**
     * 器械包名称
     */
    private String packName;

    /**
     * 申领/归属科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    /**
     * 申领/归属科室名称
     */
    private String deptName;

    /**
     * 灭菌方式（1-高压蒸汽 2-环氧乙烷 3-低温等离子）
     */
    private Integer sterilizeMethod;

    /**
     * 包状态（1-已回收 2-清洗中 3-已打包 4-灭菌中 5-待发放 6-已发放）
     */
    private Integer status;

    /**
     * 最近灭菌锅次
     */
    private String sterilizerNo;
    /**
     * 灭菌批次号
     */
    private String batchNo;
    /**
     * 最近流转时间
     */
    private LocalDateTime lastNodeTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
}
