package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CSSD 追溯节点实体（86 号脚本新增，只增不改——全链路留痕）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_cssd_trace")
public class BizCssdTrace extends BaseEntity {

    /**
     * 器械包ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long packId;
    /**
     * 器械包条码
     */
    private String packNo;

    /**
     * 节点类型（1-回收 2-清洗 3-打包 4-灭菌 5-储存 6-发放）
     */
    private Integer nodeType;

    /**
     * 节点时间
     */
    private LocalDateTime nodeTime;
    /**
     * 操作人
     */
    private String operatorName;
    /**
     * 灭菌锅次
     */
    private String sterilizerNo;
    /**
     * 灭菌批次号
     */
    private String batchNo;

    /**
     * 节点结果（1-合格 2-不合格）
     */
    private Integer result;

    /**
     * 备注
     */
    private String remark;
}
