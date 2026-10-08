package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 患者主索引合并审计（P5.1 EMPI）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_patient_merge_log")
public class BizPatientMergeLog extends BaseEntity {

    /**
     * 合并流水号（HB + yyyyMMdd + 4位序号）
     */
    private String mergeNo;

    /**
     * 主档患者ID（保留的这个）
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
     * 被并入的患者ID（合并后在册状态失效）
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
     * 匹配置信级别：1-身份证号相同 2-姓名+性别+出生日期相同 3-姓名+手机号相同 4-人工判定
     */
    private Integer matchType;
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
     * 合并时两档各自关联业务数据量快照 JSON
     */
    private String dataCount;

    /**
     * 合并理由（人工填写，必填）
     */
    private String reason;
    /**
     * 操作人ID（员工ID，不是用户的ID）
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
}
