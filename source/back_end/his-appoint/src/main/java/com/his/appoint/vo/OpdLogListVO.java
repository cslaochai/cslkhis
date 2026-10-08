package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门诊日志列表行。
 */
@Data
public class OpdLogListVO {


    /**
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 就诊号
     */
    private String registNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 挂号时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime registTime;

    /**
     * 挂号状态（1 已挂号 2 已签到 3 已接诊 4 已就诊 5 已退号 6 已过号）
     * —— 挂号域自己的口径，不要和 queueStatus 混用。
     */
    private Integer registStatus;

    /**
     * 是否已生成挂号收费单（<b>不等于已缴费</b>：收费状态在 his-charge，本模块不跨依赖）
     */
    private Boolean charged;

    // 患者

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
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;
    /**
     * 年龄
     */
    private Integer age;

    // 科室 / 医生 / 号别

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
     * 诊室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomId;
    /**
     * 诊室名称
     */
    private String roomName;

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
     * 挂号类型（1-普通号 2-专家号 3-急诊号 4-免费号）
     */
    private Integer registType;

    /**
     * 挂号来源（1 窗口 2 自助机 3 网上 4 预约）
     */
    private Integer registSource;

    /**
     * 时段（HH:mm，30 分钟粒度）
     */
    private String slotTime;

    /**
     * 结算方式（1 自费 2 城镇职工医保 3 城乡居民医保 4 公费 5 商业保险）
     */
    private Integer settlementType;

    /**
     * 医保类型（文字，取自挂号快照）
     */
    private String medicalInsuranceType;

    /**
     * 就诊类型（1 初诊 2 复诊）。取值列是挂号信息的就诊类型。
     * <p>
     * 注意别被列名带偏：库里另有一列 revisit_type（注释同样写「1-初诊 2-复诊」），
     * 但写侧从不写它、全表都是 NULL —— 用那一列会让筛选筛空、列显示「未标注」。
     * 空值仍然按「未标注」渲染，不回落成初诊或复诊。
     */
    private Integer revisitType;


    /**
     * 队列ID；为空表示还没签到入队
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long queueId;

    /**
     * 排队号（如 A-003）；为空表示还没签到入队
     */
    private String queueNo;

    /**
     * 队列原始状态码（2 候诊中 3 就诊中 4 已就诊 5 已退号 6 已过号）。
     * 库里存在码 1（列默认值，队列枚举未定义），前端据此渲染「未知(1)」。
     */
    private Integer queueStatus;

    /**
     * 队列类型（1-普通队列 2-优先队列 3-过号队列）
     */
    private Integer queueType;

    /**
     * 顺序号
     */
    private Integer sequenceNo;
    /**
     * 叫号次数
     */
    private Integer callCount;

    /**
     * 叫号时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime callTime;

    /**
     * 到达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime arriveTime;

    /**
     * 开始就诊时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束就诊时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 是否过号（0-否 1-是）
     */
    private Integer isOverdue;

    /**
     * 过号时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime overdueTime;

    /**
     * 过号原因
     */
    private String overdueReason;

    // 现算时长（分钟）

    /**
     * 候诊时长 = 开始就诊 - 到达（start/arrive 任一为空则为 null）
     */
    private Integer waitMinutes;

    /**
     * 就诊时长 = 结束 - 开始（start/end 任一为空则为 null）
     */
    private Integer visitMinutes;

    // 就诊状态（推导，见 OpdLogStatusEnum）

    /**
     * 就诊状态码：0 未缴费 / 1 待签到 / 2 候诊中 / 3 就诊中 / 4 已就诊 / 5 已退号 / 6 已过号。
     *
     * <p>由「挂号状态 + 队列状态」共同推导（规则见 {@code OpdLogStatusEnum}）：
     * 已入队时队列状态优先；未入队时回落到挂号状态（这就是为什么
     * {@code queueStatus} 为 null 的行也能是「已退号」，不是数据缺失）。
     *
     * <p>为 null 表示两种口径都认不出（如队列状态为历史脏数据 1），
     * 前端必须渲染成「未知(queueStatus)」。
     */
    private Integer logStatus;

    /**
     * 就诊状态文案（由后端给，避免前端各写一份）
     */
    private String logStatusLabel;
}
