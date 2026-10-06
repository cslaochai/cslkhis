package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 透析机位台账。
 *
 * <p>机位是透析室专有资源，不复用床位（住院床位有病区/占用语义，混用会把透析排班
 * 和住院分床搅在一起）。机位号唯一，维修/停用不可排班。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dialysis_machine")
public class BizDialysisMachine extends BaseEntity implements Serializable {

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
