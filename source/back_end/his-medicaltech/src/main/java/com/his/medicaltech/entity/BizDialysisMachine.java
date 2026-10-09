package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 透析机位台账。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dialysis_machine")
public class BizDialysisMachine extends BaseEntity implements Serializable {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 机位号
     */
    private String machineNo;

    /**
     * 透析分区
     */
    private String roomName;

    /**
     * 状态（1-可用 2-维修 3-停用）
     */
    private Integer status;
}
