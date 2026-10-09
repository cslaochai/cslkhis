package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 设备可开展项目（设备可开展项目）—— 预约路由的唯一事实源。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exam_device_item")
public class BizExamDeviceItem extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 检查项目ID
     */
    private Long itemId;

    /**
     * 项目编码
     */
    private String itemCode;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 该设备做该项目的时长（分钟），空则取项目字典 duration
     */
    private Integer examMinutes;
}
