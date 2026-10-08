package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备可开展项目（设备可开展项目）—— 预约路由的唯一事实源。
 *
 * <p>不用检查项目字典的科室ID 路由：实测该列 105 行全是孤儿引用
 * （值 101~106，科室里没有这些 id），且设备与项目本来就是多对多。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exam_device_item")
public class BizExamDeviceItem extends BaseEntity {

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
