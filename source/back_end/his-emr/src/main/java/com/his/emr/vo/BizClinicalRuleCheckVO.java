package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 临床规则校验记录出参
 */
@Data
public class BizClinicalRuleCheckVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 校验单号
     */
    private String checkNo;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 规则类型（1-配伍禁忌 2-检验诊断关联性 3-用药合理性）
     */
    private Integer ruleType;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则内容
     */
    private String ruleContent;

    /**
     * 校验结果：1-通过 2-不通过
     */
    private Integer checkResult;

    /**
     * 错误级别：1-提示 2-警告 3-严重
     */
    private Integer errorLevel;

    /**
     * 错误详情
     */
    private String errorDetail;

    /**
     * 处理建议
     */
    private String suggestion;

    /**
     * 处理状态（1-待处理 2-已处理 3-已忽略）
     */
    private Integer checkStatus;

    /**
     * 校验人
     */
    private String checkBy;

    /**
     * 校验时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

}
