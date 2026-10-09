package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 候诊队列
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_queue")
public class BizQueue extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 排队序号
     */
    private String queueNo;

    /**
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 队列类型（1-普通队列 2-优先队列 3-过号队列）
     */
    private Integer queueType;

    /**
     * 排队状态（2-候诊中 3-就诊中 4-已就诊 5-已退号 6-已过号 7-已失效）
     */
    private Integer queueStatus;

    /**
     * 就诊日期（口径同挂号信息的就诊日期）。
     * callNext 的当日过滤、签到的重复判断都按它来 ——
     * 原先用 arrive_time 截日期，跨日时会把昨天的患者叫出来。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 分诊状态（0-未经护士核验 1-已核验）。
     * <p>签到入队时写 0 并同时给 4 级默认等级，所以 0 不再意味「不能接诊」，
     * 只表示这份分级还没经分诊护士看过。
     */
    private Integer triageStatus;

    /**
     * 分诊等级（1-危重 2-急症 3-亚急 4-非急），数字越小越优先。
     */
    private Integer triageLevel;

    /**
     * 分配诊室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;

    /**
     * 分配诊室名称
     */
    private String roomName;

    /**
     * 顺序号
     */
    private Integer sequenceNo;

    /**
     * 叫号时间
     */
    private LocalDateTime callTime;

    /**
     * 叫号次数
     */
    private Integer callCount;

    /**
     * 到达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arriveTime;

    /**
     * 开始就诊时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束就诊时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 等待时长（分钟）
     */
    private Integer waitDuration;

    /**
     * 是否过号（0-否 1-是）
     */
    private Integer isOverdue;

    /**
     * 过号时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime overdueTime;

    /**
     * 过号原因
     */
    private String overdueReason;

    /**
     * 挂号类型（1-普通号 2-专家号 3-急诊号 4-免费号）
     */
    private Integer registType;

    /**
     * 支付状态（非数据库字段）
     */
    @TableField(exist = false)
    private Integer paymentStatus;

    /**
     * 挂号单号（非数据库字段）
     */
    @TableField(exist = false)
    private String registNo;
}
