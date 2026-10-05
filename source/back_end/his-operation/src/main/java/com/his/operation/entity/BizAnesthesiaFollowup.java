package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 麻醉术后随访单（麻醉术后随访单）。
 *
 * <p>挂在<b>麻醉记录</b>上（不是手术申请）：随访评的是"麻醉这个人恢复得怎么样"，
 * 一台麻醉可以有多轮随访（术后即刻 / 24h / 48h，或并发症追加），所以
 * 记录ID 只建普通索引，不设唯一键。
 *
 * <p>草稿（0）可改可删，完成（1）即锁死 —— 与麻醉记录"提交后不可补体征"同一原则：
 * 随访的价值是"当时看到的是什么样"，事后润色等于伪造。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_anesthesia_followup")
public class BizAnesthesiaFollowup extends BaseEntity {

    /**
     * 随访单号（MS + yyyyMMdd + 4位序号）
     */
    private String followupNo;

    /**
     * 麻醉记录ID（必须已提交/已审核）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 麻醉记录单号（快照）
     */
    private String recordNo;

    /**
     * 手术申请单ID（快照，从麻醉记录带出）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 性别（快照）（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄（快照）
     */
    private Integer age;

    /**
     * 随访时间（不得早于麻醉结束时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followupTime;

    /**
     * 随访轮次（1-术后即刻 2-24h 3-48h及以后）
     */
    private Integer roundNo;

    /**
     * 疼痛评分 NRS 0~10
     */
    private Integer painScore;

    /**
     * 麻醉恢复情况（1-良好 2-一般 3-差）
     */
    private Integer recovery;

    /**
     * 麻醉并发症码值（逗号分隔，见 FollowupAdverseItems；无并发症为空）
     */
    private String adverseItems;

    /**
     * 并发症经过描述（勾选任何一项时必填）
     */
    private String adverseNote;

    /**
     * 处理措施与转归（有并发症时必填）
     */
    private String handling;

    /**
     * 状态：0-草稿 1-已完成（完成即锁死）
     */
    private Integer followupStatus;

    /**
     * 随访麻醉医师ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long followupDoctorId;

    /**
     * 随访麻醉医师姓名（快照）
     */
    private String followupDoctorName;

    /**
     * 随访完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;
}
