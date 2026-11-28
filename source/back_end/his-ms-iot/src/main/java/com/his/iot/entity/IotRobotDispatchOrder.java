package com.his.iot.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 物流机器人配送任务单（biz_robot_dispatch_order）。
 * <p>
 * 车队全院共享调度：科室/楼层只是收发站点，机器人按空闲状态、电量、优先级统一派车。
 * 状态流转：待调度(1) → 已派单(2) → 配送中(3) → 已签收(4)；异常分支 已取消(5)/异常(6)。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_robot_dispatch_order")
public class IotRobotDispatchOrder extends BaseEntity {

    /** 配送单号（RB+yyyyMMdd+5位序列） */
    private String orderNo;

    /** 发起来源：PIVAS/PHARMACY/LAB/WAREHOUSE/CSSD/LAUNDRY/MANUAL */
    private String sourceType;

    /** 发起科室/站点名称快照 */
    private String sourceDeptName;

    /** 发起人员工ID */
    private Long requesterId;

    /** 发起人姓名快照 */
    private String requesterName;

    /** 品类：药品/标本/耗材/器械包/被服 */
    private String goodsType;

    /** 物品明细 */
    private String goodsName;

    /** 取货点 */
    private String fromStation;

    /** 目标楼层 */
    private Integer toFloor;

    /** 收货站点（护士站） */
    private String toStation;

    /** 优先级：1-常规 2-批量补货 3-急送 */
    private Integer priority;

    /** 箱数 */
    private Integer boxCount;

    /** 本单总重(kg) */
    private Double loadKg;

    /** 状态：1-待调度 2-已派单 3-配送中 4-已签收 5-已取消 6-异常 */
    private Integer status;

    /** 分配机器人编号 */
    private String robotNo;

    /** 签收人 */
    private String signer;

    /** 派单人姓名 */
    private String dispatchBy;

    /** 派单人员工ID */
    private Long dispatchById;

    /** 派单时间 */
    private LocalDateTime dispatchTime;

    /** 签收完成时间 */
    private LocalDateTime completeTime;

    /** 全程用时(秒) */
    private Integer totalSec;

    /** 乘梯用时(秒，含排队) */
    private Integer elevatorSec;

    /** 梯口排队(秒) */
    private Integer queueSec;

    /** 轿厢运行(秒) */
    private Integer rideSec;

    /** 本单里程(米) */
    private Double odometerM;

    /** 取消原因 */
    private String cancelReason;

    /** 备注 */
    private String remark;

    /** 删除标志 */
    @TableLogic
    private Integer delFlag;
}
