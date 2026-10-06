package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警记录（欠费等业务提醒的落点，P3 用它记住院欠费）。
 *
 * <p>欠费<b>只提示不阻断</b>：急救场景不允许因为欠费卡住医嘱与执行，
 * 所以这里只留痕"提醒过谁、什么时候、提醒内容是什么"，由前端去展示。
 * 与「发送方 status=1 只证明我发过」同一条口径：记下来的是"发过提醒"，不是"已缴费"。
 */
@Data
@TableName("biz_alert")
public class BizAlert implements Serializable {

    /**
     * 告警ID
     */
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long alertId;

    /**
     * 告警单号（BJ + yyyyMMdd + 4位序号）
     */
    private String alertNo;

    /**
     * 关联规则ID（可空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ruleId;

    /**
     * 告警类型：ARREARS-欠费
     */
    private String alertType;

    /**
     * 告警内容
     */
    private String alertContent;

    /**
     * 状态（0-未处理 1-已处理 2-已忽略）
     */
    private Integer alertStatus;

    /**
     * 通知对象（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long notifyUserId;

    /**
     * 通知时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime notifyTime;

    /**
     * 读取时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    /**
     * 备注
     */
    private String remark;
}
