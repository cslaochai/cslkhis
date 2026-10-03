package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 床位匹配候选
 *
 * <p><b>为什么给分而不是给一个"最优解"</b>：床位调配是人在做决定 ——
 * 护士长知道三床旁边那张空床的氧气带坏了、知道某张床昨天刚腾出来还没终末消毒，
 * 这些系统不知道。系统的职责是"把符合硬条件的摆出来并说清楚为什么符合"，
 * 剩下的判断留给现场。<b>给一个最优解让人一键确认，就是把人变成橡皮图章。</b>
 *
 * <p>genderHint / isolationHint 是<b>提示不是过滤</b>：本项目床位上没有"同病房性别"这类属性，
 * 硬过滤等于假装数据有而实际上没有。诚实做法是标出来让人看一眼。
 */
@Data
public class BedMatchVO {

    /** 床位ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /** 床位号 */
    private String bedNo;

    private String bedType;

    private String bedTypeText;

    /** 病区ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 病区名称（快照） */
    private String wardName;

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称 */
    private String deptName;

    /** 匹配档位：1-本科室同床型 2-本科室可降级 3-跨科同床型 4-跨科可降级 */
    private Integer matchLevel;

    private String matchLevelText;

    /** 综合得分（越高越适合，仅用于排序） */
    private Integer matchScore;

    /** 一句话说明为什么推荐这张床 */
    private String matchReason;

    /** 与期望病区一致 */
    private Boolean expectWardMatched;

    /** 患者性别限制在此床上需要人工确认 */
    private Boolean genderHint;

    /** 隔离需求在此床上需要人工确认 */
    private Boolean isolationHint;
}
