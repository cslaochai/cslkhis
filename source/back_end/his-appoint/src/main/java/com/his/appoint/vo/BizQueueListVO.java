package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排队信息VO
 */
@Data
public class BizQueueListVO {

    /**
     * 队列ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 排队序号
     */
    private String queueNo;

    /**
     * 挂号记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（0-未知 1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 挂号单号
     */
    private String registNo;

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
     * 顺序号
     */
    private Integer sequenceNo;

    /**
     * 叫号时间
     * <p>⚠ 这批 LocalDateTime 必须显式 @JsonFormat：不加时 Jackson 输出 ISO
     * （{@code 2026-09-20T16:34:25}），而前端为了兼容老 Safari 习惯做
     * {@code replace(/-/g,'/')} —— 替换后变成 {@code 2026/09/20T16:34:25}，
     * 分隔符混用直接 Invalid Date，分诊台「等候」列就显示成「NaN时NaN分」。
     * 统一成 {@code yyyy-MM-dd HH:mm:ss} 与 createTime 同口径。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime callTime;

    /**
     * 叫号次数
     */
    private Integer callCount;

    /**
     * 到达时间（见 callTime 的说明：必须显式 @JsonFormat）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime arriveTime;

    /**
     * 开始就诊时间（见 callTime 的说明）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束就诊时间（见 callTime 的说明）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
     * 过号时间（见 callTime 的说明）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime overdueTime;

    /**
     * 过号原因
     */
    private String overdueReason;

    /**
     * 是否复诊（0-否 1-是）
     */
    private Integer visitType;

    /**
     * 挂号类型（1-普通号 2-专家号 3-急诊号 4-免费号）
     */
    private Integer registType;

    /**
     * 挂号单状态（AppointStatusEnum：1已挂号 ... 4已就诊/结诊 ...）。
     * 队列 4-已就诊有两种来路：医生点了「结诊」（挂号单同步置 4）或只是叫下一位被
     * 自动收口（挂号单还是已挂号）。前端「回诊」按钮只该出现在后者 ——
     * 结诊=病历已提交归档，再改要走修改留痕，行业口径是挂复诊号而不是回诊。
     */
    private Integer registStatus;

    /**
     * 结算方式（1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-商业保险）
     */
    private Integer settlementType;

    /**
     * 医保类型
     */
    private String medicalInsuranceType;

    /**
     * 就诊日期（同挂号信息的就诊日期）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 分诊状态（0-未经护士核验 1-已核验）
     */
    private Integer triageStatus;

    /**
     * 分诊等级（1-危重 2-急症 3-亚急 4-非急）。签到入队即写 4，仅历史脏数据为 null
     */
    private Integer triageLevel;

    /**
     * 分诊等级文案，由后端统一给，前端不自拼
     */
    private String triageLevelText;

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
     * 复诊关联的原病历ID（批次E/E6；仅 visitType=2 有值）。
     * <p>医生站据此把复诊患者和上一次的病历对上——只表示引用关系，原病历不改。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long revisitRecordId;
}
