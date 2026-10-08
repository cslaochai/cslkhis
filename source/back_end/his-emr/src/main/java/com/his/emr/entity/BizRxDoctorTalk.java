package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医师约谈记录（对开具不合理处方医师的约谈/警告/限制/取消处方权台账）。
 */
@Data
@TableName("biz_rx_doctor_talk")
public class BizRxDoctorTalk implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 约谈编号（YT+yyyyMMdd+4位序号）
     */
    private String talkNo;

    /**
     * 被约谈医师ID（从关联点评明细推出；独立约谈可空）
     */
    private Long doctorId;

    /**
     * 被约谈医师姓名
     */
    private String doctorName;

    /**
     * 医师所在科室
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
     * 约谈部门（医务科/药学部）
     */
    private String talkerOrg;

    /**
     * 关联不合理处方数
     */
    private Integer relatedCount;

    /**
     * 关联点评明细ID（逗号分隔，约谈依据）
     */
    private String relatedReviewIds;

    /**
     * 问题摘要（约谈事由）
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
     * 医师确认（0未确认 1已确认；确认后禁改禁删）
     */
    private Integer doctorConfirm;

    /**
     * 医师确认人（模拟签字）
     */
    private String doctorConfirmBy;

    /**
     * 医师确认时间
     */
    private LocalDateTime doctorConfirmTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}
