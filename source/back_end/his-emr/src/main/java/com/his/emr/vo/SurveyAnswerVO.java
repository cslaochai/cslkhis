package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 满意度答卷（列表 + 详情共用；详情带逐题答案）。
 */
@Data
public class SurveyAnswerVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 答卷编号
     */
    private String answerNo;

    /**
     * 发放单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dispatchId;

    /**
     * 模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    private String templateName;

    /**
     * 场景
     */
    private Integer scene;

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
     * 就诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 李克特均分
     */
    private BigDecimal avgScore;

    private BigDecimal score100;

    /**
     * NPS 推荐度
     */
    private Integer nps;

    /**
     * 开放意见
     */
    private String commentText;

    /**
     * 填报方式（1-患者自填 2-随访员代填 3-现场扫码）
     */
    private Integer fillSource;

    /**
     * 是否匿名（0-否 1-是）
     */
    private Integer anonymousFlag;

    /**
     * 代填人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fillEmployeeId;

    /**
     * 代填人姓名
     */
    private String fillEmployeeName;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fillTime;

    /**
     * 状态（1-有效 2-已作废）
     */
    private Integer answerStatus;

    /**
     * 低分自动转出的投诉单（非空=已转过，前端按钮变「查看投诉单」）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long disputeCaseId;

    /**
     * 可重填（有效卷且尚未转投诉 —— 改分数把投诉的由来改掉是不可接受的）
     */
    private Boolean canEdit;

    /**
     * 可作废（有效卷）
     */
    private Boolean canVoid;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逐题答案（仅详情接口返回）
     */
    private List<SurveyAnswerItemVO> items;
}
