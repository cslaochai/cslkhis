package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 满意度发放/回收台账行（列表与详情共用）。
 */
@Data
public class SurveyDispatchVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 发放单号
     */
    private String dispatchNo;

    /**
     * 问卷模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /**
     * 模板名称
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
     * 脱敏手机号（列表用）
     */
    private String phoneMasked;

    /**
     * 明文手机号（仅编辑回显接口返回）
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pushTime;

    /**
     * 回收截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 回收到的答卷ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long answerId;

    /**
     * 是否已超截止未回收（服务端现算，不靠定时任务翻 dispatch_status）
     */
    private Boolean overdue;

    /**
     * 可回收录入（未回收都能填，拒答后患者改主意也允许填回来）
     */
    private Boolean canFill;

    /**
     * 可标记已推送
     */
    private Boolean canPush;

    /**
     * 可标记拒答
     */
    private Boolean canRefuse;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
