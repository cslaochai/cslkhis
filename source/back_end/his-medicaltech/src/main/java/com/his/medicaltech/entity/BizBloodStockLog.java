package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 血库出入库流水（血库出入库流水）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_blood_stock_log")
public class BizBloodStockLog extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 血袋号
     */
    private String bagNo;

    /**
     * 业务类型（1-入库 2-发血 3-退回 4-报废 5-预留 6-取消预留）
     */
    private Integer bizType;

    /**
     * 变更前状态
     */
    private Integer fromStatus;

    /**
     * 变更后状态
     */
    private Integer toStatus;

    /**
     * 关联用血申请单号
     */
    private String applyNo;

    /**
     * 原因
     */
    private String reason;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;
}
