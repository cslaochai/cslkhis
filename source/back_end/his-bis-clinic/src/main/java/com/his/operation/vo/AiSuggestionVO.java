package com.his.operation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 排台建议/受阻行。
 *
 * <p>口径：单条建议给「最早可用空档」（贪心：急诊优先、按申请时间先后，
 * 同手术间与已排台时段不重叠、营业窗口 08:00~18:00 内、已定主刀/麻醉/护士不分身）；
 * 建议时长 = 按手术级别预估（1级60/2级90/3级120/4级180 分钟，未定级别 90）。
 * 采纳由前端调既有 schedule 接口落库，本行只出建议不落库。
 * 主键 id 全部字符串化出参（JS 精度），采纳回传时 Jackson 可反序列化回 Long。
 */
@Data
public class AiSuggestionVO {

    /** 手术申请单 id */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /** 手术单号 */
    private String applyNo;

    /** 患者姓名 */
    private String patientName;

    /** 拟施术式 */
    private String plannedOperationName;

    /** 手术级别（1-4） */
    private Integer operationLevel;

    /** 是否急诊（1-是 0-否） */
    private Integer isEmergency;

    /** 申请科室 */
    private String applyDeptName;

    /** AI 预估时长（分钟） */
    private Integer durationMinutes;

    /** 原单主刀医师 id（可为空，采纳时带回） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long surgeonId;

    /** 原单主刀姓名 */
    private String surgeonName;

    /** 原单麻醉医师 id（可为空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long anesthetistId;

    /** 原单麻醉姓名 */
    private String anesthetistName;

    /** 原单巡回护士 id（可为空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long circulatingNurseId;

    /** 原单洗手护士 id（可为空） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scrubNurseId;

    /** 建议手术间名称（blocked 行为空） */
    private String suggestedRoomName;

    /** 建议开始时间（blocked 行为空） */
    private LocalDateTime suggestedStart;

    /** 建议结束时间（blocked 行为空） */
    private LocalDateTime suggestedEnd;

    /** 建议依据 / 受阻原因文案 */
    private String reason;
}
