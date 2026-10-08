package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 患者合并审计条目（P5.1 EMPI）
 */
@Data
public class PatientMergeLogVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 合并流水号
     */
    private String mergeNo;

    /**
     * 主档患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long masterId;
    /**
     * 主档患者号
     */
    private String masterNo;
    /**
     * 主档姓名
     */
    private String masterName;

    /**
     * 被并入的患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long mergedId;
    /**
     * 被并患者号
     */
    private String mergedNo;
    /**
     * 被并姓名
     */
    private String mergedName;

    /**
     * 匹配置信级别(强)（1-身份证号相同 2-姓名+性别+出生日期相同 3-姓名+手机号相同 4-人工判定）
     */
    private Integer matchType;
    private String matchTypeText;
    /**
     * 命中依据的字段值快照
     */
    private String matchSnapshot;
    /**
     * 主档关键字段快照 JSON
     */
    private String masterSnapshot;
    /**
     * 被并档关键字段快照 JSON
     */
    private String mergedSnapshot;
    /**
     * 合并时两档数据量快照 JSON（"并了多少数据"要有纸面记录）
     */
    private String dataCount;

    /**
     * 合并理由
     */
    private String reason;

    /**
     * 操作人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;
    /**
     * 操作人姓名
     */
    private String operatorName;
    /**
     * 合并时间
     */
    private LocalDateTime mergeTime;

    /**
     * 状态（1-已合并 2-已撤销）
     */
    private Integer logStatus;
    private String logStatusText;
    /**
     * 撤销人
     */
    private String revertBy;
    /**
     * 撤销时间
     */
    private LocalDateTime revertTime;
    /**
     * 撤销理由
     */
    private String revertReason;

    /**
     * 是否还可撤销（已撤销 / 被并档已被别的操作改动时为 false）
     */
    private Boolean canRevert;
}
