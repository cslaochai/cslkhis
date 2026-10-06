package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检查预约设备档位（检查设备档位）
 *
 * <p>与医疗设备台账的分工：那边是资产台账（G22 的档案/维保/计量），这边是「可预约的运行资源」档位
 * （开放时段、号源粒度、并行数）。equipment_id 只是挂接，台账缺行时留空 —— 预约域不依赖台账能否跑。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exam_device")
public class BizExamDevice extends BaseEntity {

    /**
     * 预约设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类别（1-CT 2-MR 3-DR/CR 4-超声 5-心电 6-内镜 7-其他）
     */
    private Integer deviceType;

    /**
     * 设备台账ID（医疗设备台账的ID，可空）
     */
    private Long equipmentId;

    /**
     * 检查科室ID
     */
    private Long deptId;

    /**
     * 检查科室名称
     */
    private String deptName;

    /**
     * 检查室/机房位置
     */
    private String roomName;

    /**
     * 上午开放开始（HH:mm）
     */
    private String amStart;

    /**
     * 上午开放结束（HH:mm）
     */
    private String amEnd;

    /**
     * 下午开放开始 HH:mm（空=只排上午）
     */
    private String pmStart;

    /**
     * 下午开放结束（HH:mm）
     */
    private String pmEnd;

    /**
     * 号源粒度（分钟）
     */
    private Integer slotMinutes;

    /**
     * 单格子并行号数
     */
    private Integer parallelCount;

    /**
     * 可提前预约天数
     */
    private Integer aheadDays;

    /**
     * 可占号最长时长（分钟），超过的项目拒绝走号源池
     */
    private Integer maxSlotMinutes;

    /**
     * 状态（1-开放预约 2-暂停预约）
     */
    private Integer status;
}
