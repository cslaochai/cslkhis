package com.his.operation.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 麻醉随访出参。
 *
 * <p>惯例同 {@link OperationApplyVO}：ID 字符串化、码值带 xxxText、
 * 按钮可用性（canEdit/canFinish/canDelete）由服务端按状态算。
 */
@Data
public class AnesthesiaFollowupVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 随访单号（MSyyyyMMddnnnn）
     */
    private String followupNo;

    /**
     * 麻醉记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 麻醉记录单号
     */
    private String recordNo;

    /**
     * 手术申请单ID
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
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女）
     */
    private Integer gender;

    private String genderText;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 随访时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime followupTime;

    /**
     * 随访轮次（1-术后即刻 2-24h 3-48h及以后）
     */
    private Integer roundNo;

    /**
     * 轮次文案（第2轮·术后24h）
     */
    private String roundText;

    /**
     * 疼痛评分 NRS 0~10
     */
    private Integer painScore;

    /**
     * 麻醉恢复情况（1-良好 2-一般 3-差）
     */
    private Integer recovery;

    private String recoveryText;

    /**
     * 并发症码值串（原始）
     */
    private String adverseItems;

    /**
     * 并发症文案（"恶心呕吐、尿潴留"；空 = 无并发症）
     */
    private String adverseItemsText;

    /**
     * 并发症经过描述
     */
    private String adverseNote;

    /**
     * 处理措施与转归
     */
    private String handling;

    /**
     * 状态（0-草稿 1-已完成）
     */
    private Integer followupStatus;

    private String followupStatusText;

    /**
     * 随访麻醉医师ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long followupDoctorId;

    /**
     * 随访麻醉医师姓名
     */
    private String followupDoctorName;

    /**
     * 随访完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /**
     * 备注
     */
    private String remark;

    // 服务层算出的可用性字段（草稿才可改/删/完成）

    private Boolean canEdit;

    private Boolean canFinish;

    private Boolean canDelete;

    /**
     * 并发症要点字典（仅详情返回，前端渲染勾选框）
     */
    private List<OperationApplyVO.CheckItem> adverseItemOptions;
}
