package com.his.operation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 手术完成入参：填写术中/术后记录，并触发回写（病案首页手术明细 + record_type=5 手术记录）。
 *
 * <p><b>必填的六项，每一项都对应一个会被追问的问题</b>：
 * <ul>
 *   <li>{@code actualOperationName} —— 与拟施不一致时，首页要记的是**实际做的**那个
 *       （高编高套最常见的手法就是"只做了探查却编切除术"，首页写拟施就是帮忙造假）；</li>
 *   <li>{@code operationStartTime / operationEndTime} —— 没有起止时间的手术时长算不出来，
 *       "手术时长"是首页和绩效都要的字段；</li>
 *   <li>{@code intraopFindings} —— 术中所见，四核对里"病历有没有支持这段编码的描述"的正文；</li>
 *   <li>{@code intraopProcedure} —— 手术经过；</li>
 *   <li>{@code postopNote} —— 术后处理，缺了等于"做完就不管了"。</li>
 * </ul>
 *
 * <p>出血量、标本送检可为空（不是每台手术都有），但不允许把 0 当成"没填"：
 * 0 ml 出血是一台真实存在的手术的观测值。
 */
@Data
public class OperationFinishDTO implements Serializable {

    /** 手术申请单ID（必填） */
    @NotNull(message = "手术申请单ID不能为空")
    private Long applyId;

    /** 实际手术编码（可空） */
    private String actualOperationCode;

    /** 实际手术名称（必填） */
    @NotBlank(message = "实际手术名称不能为空（与拟施不一致时，首页记的是实际做的那个）")
    private String actualOperationName;

    /** 实际开始时间（必填）。{@code @JsonFormat} 不能省，理由同排台 DTO：默认只认 ISO 格式 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "实际开始/结束时间不能为空（没有起止时间算不出手术时长）")
    private LocalDateTime operationStartTime;

    /** 实际结束时间（必填，必须晚于开始时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "实际开始/结束时间不能为空（没有起止时间算不出手术时长）")
    private LocalDateTime operationEndTime;

    /** 术中出血量（ml，可空；0 是合法观测值） */
    private Integer bloodLoss;

    /** 术中所见（必填） */
    @NotBlank(message = "术中所见不能为空（编码能不能被病历支持，追的就是这一段）")
    private String intraopFindings;

    /** 手术经过/操作步骤（必填） */
    @NotBlank(message = "手术经过不能为空")
    private String intraopProcedure;

    /** 术后处理与注意事项（必填） */
    @NotBlank(message = "术后处理不能为空（缺了等于「做完就不管了」）")
    private String postopNote;

    /** 标本送检（可空；无标本请写"无"，不要留空 —— 留空会被读成"忘了送"） */
    private String specimenSent;

    /** 备注 */
    private String remark;
}
