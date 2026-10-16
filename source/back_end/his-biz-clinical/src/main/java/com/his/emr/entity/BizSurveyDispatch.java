package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 满意度评价发放与回收台账（sql/164）——「评价回收这张脸」。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_survey_dispatch")
public class BizSurveyDispatch extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 回收截止天数（发放后 N 天未回收即视为过期）
     */
    public static final int DEFAULT_EXPIRE_DAYS = 14;

    /**
     * 发放单号（SD+yyyyMMdd+4位）
     */
    private String dispatchNo;

    /**
     * 问卷模板ID
     */
    private Long templateId;

    /**
     * 模板名称（快照，模板改名不影响已发放台账）
     */
    private String templateName;

    /**
     * 适用场景
     */
    private Integer scene;

    /**
     * 发放来源（1-随访任务 2-出院结算 3-人工补发）
     */
    private Integer sourceType;

    /**
     * 来源单据ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;

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
     * 联系手机号（明文存供触达；列表出参一律脱敏）
     */
    private String phone;

    /**
     * 就诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 回收渠道（1-电话代填 2-短信 3-微信 4-现场扫码）
     */
    private Integer channel;

    /**
     * 回收状态（1-待推送 2-已推送待回收 3-已回收 4-已过期 5-已拒答）
     */
    private Integer dispatchStatus;

    /**
     * 推送/发起时间
     */
    private LocalDateTime pushTime;

    /**
     * 回收截止时间
     */
    private LocalDateTime expireTime;

    /**
     * 回收到的答卷ID（一发放一答卷）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long answerId;
}
