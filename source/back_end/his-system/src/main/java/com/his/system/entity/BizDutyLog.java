package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 全院总值班日志（交班本）。
 *
 * <p>sql/169 回答了「今天全院谁负责」，但值班这件事在评审面前还有后半句：
 * <b>值班期间发生了什么、没处理完的交给谁了、接班人收到没有</b>。本表就是这后半句。
 *
 * <p>一条 = 一件事（值班事件 / 遗留事项 / 巡查记录），按 {@code duty_date + shift_type} 归班。
 * 不做「一个班次一大段文本」—— 大文本框的下场一定是空的，而且事后查不到"上周那次突发"。
 *
 * <p>两条硬口径：
 * <ol>
 *   <li><b>值班人 ≠ 记录人</b>：员工ID 是这事归谁值班，创建人是谁写的。
 *       代记、补记必须留痕 —— 把两者合成一列，事后就分不清"他值班时出的事"还是"他随手记的"。</li>
 *   <li><b>交接以签收为终点</b>：交班只把状态推到 2（已交班），接班人 {@code ack} 之后才是 3。
 *       只看"交班时间"不算交接完成 —— 交出去没人收，等于没交。</li>
 * </ol>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_duty_log")
public class BizDutyLog extends BaseEntity {

    /**
     * 值班日期（与全院总值班排班同口径：夜班归开始日）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dutyDate;

    /**
     * 班次：1-白班 2-夜班（字典 his_duty_shift）
     */
    private Integer shiftType;

    /**
     * 所属排班行全院总值班排班的ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long rosterId;

    /**
     * 值班人（记录归属；代记时 ≠ create_by）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 值班人姓名
     */
    private String employeeName;

    /**
     * 记录类型：1-值班事件 2-遗留事项 3-巡查记录（字典 his_duty_log_type）
     */
    private Integer logType;

    /**
     * 事件发生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime happenTime;

    /**
     * 标题（一句话说清是什么事）
     */
    private String title;

    /**
     * 事件经过
     */
    private String content;

    /**
     * 处理情况
     */
    private String handleResult;

    /**
     * 状态：0-待处理 1-已处理 2-已交班（等签收）3-已签收（字典 his_duty_log_status）
     */
    private Integer status;

    /**
     * 接班人（交班时写入；留空 = 没人接）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long handoverEmpId;

    /**
     * 接班人姓名
     */
    private String handoverEmpName;

    /**
     * 交班时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handoverTime;

    /**
     * 接班人签收时间（有值才算交接闭环）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ackTime;
}
