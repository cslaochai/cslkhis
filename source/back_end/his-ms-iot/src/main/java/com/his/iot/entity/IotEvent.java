package com.his.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物联网设备事件（biz_iot_event，按 ts 月分区）。
 * <p>
 * MQTT 的 /event 主题全量落这张表：婴儿防盗的越区/拆带/配对丢失、输液的阻塞/余量不足都在这里，
 * 处理状态与处理人用于护士站告警闭环。幂等键 (device_no, seq, ts)。
 */
@Data
@TableName("biz_iot_event")
public class IotEvent implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 设备编号
     */
    private String deviceNo;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 归属科室ID（设备档案未落科室时为空）
     */
    private Long deptId;

    /**
     * 事件码，见 IotEventCodeEnum
     */
    private String eventCode;

    /**
     * 事件级别（1-提示 2-警告 3-严重），见 IotEventLevelEnum
     */
    private Integer level;

    /**
     * 事件描述
     */
    private String detail;

    /**
     * 事件明细 JSON（原样保留设备侧 data）
     */
    private String dataJson;

    /**
     * 幂等序号（设备重发时相同）
     */
    private Long seq;

    /**
     * 事件发生时间
     */
    private LocalDateTime ts;

    /**
     * 处理标志（0-未处理 1-已处理 2-已忽略），见 IotEventHandleStatusEnum
     */
    private Integer handledFlag;

    /**
     * 处理人ID
     */
    private Long handleById;

    /**
     * 处理人
     */
    private String handleBy;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理说明
     */
    private String handleRemark;

    /**
     * 入库时间
     */
    private LocalDateTime createTime;
}
