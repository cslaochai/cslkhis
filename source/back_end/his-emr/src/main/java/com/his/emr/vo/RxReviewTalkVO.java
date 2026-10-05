package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 医师约谈记录 VO
 */
@Data
public class RxReviewTalkVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 约谈编号
     */
    private String talkNo;

    /**
     * 被约谈医师ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 被约谈医师姓名
     */
    private String doctorName;

    /**
     * 医师所在科室（快照）
     */
    private String deptName;

    /**
     * 约谈类型（1-首次约谈 2-警告约谈 3-限制处方权 4-取消处方权 5-恢复处方权）
     */
    private Integer talkType;

    /**
     * 约谈时间
     */
    private LocalDateTime talkTime;

    /**
     * 约谈人姓名
     */
    private String talkerName;

    /**
     * 约谈部门
     */
    private String talkerOrg;

    /**
     * 关联不合理处方数
     */
    private Integer relatedCount;

    /**
     * 关联点评明细ID（逗号分隔原文，导出/留痕用）
     */
    private String relatedReviewIds;

    /**
     * 关联点评明细（结构化，前端跳转展示）
     */
    private List<RxReviewItemVO> relatedItems;

    /**
     * 问题摘要
     */
    private String problemSummary;

    /**
     * 约谈内容
     */
    private String talkContent;

    /**
     * 整改要求
     */
    private String rectifyRequire;

    /**
     * 整改状态（1-待整改 2-已整改）
     */
    private Integer rectifyStatus;

    /**
     * 整改情况说明
     */
    private String rectifyRemark;

    /**
     * 医师确认（0-未确认 1-已确认）
     */
    private Integer doctorConfirm;

    /**
     * 医师确认人
     */
    private String doctorConfirmBy;

    /**
     * 医师确认时间
     */
    private LocalDateTime doctorConfirmTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;
}
