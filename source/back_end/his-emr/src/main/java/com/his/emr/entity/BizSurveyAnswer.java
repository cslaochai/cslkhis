package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 满意度答卷主表（sql/164）——一次回收一条。
 *
 * <p>uk_survey_answer_dispatch(dispatch_id) 是**重复回收的最后一道闸**：
 * 电话里患者改主意重说一遍是常态，没有这条键，同一次发放能落两张答卷，均分当场被灌水。
 *
 * <p>作废（answer_status=2）不删行、也不新起一行 —— 本表唯一键是 dispatch_id，一张发放单永远只有一行答卷，
 * 所以「作废」是**待重填的瞬时态**而不是历史台账：重填会覆盖同一行（remark 与 dispute_case_id 一起被改写）。
 * 已转投诉的那次低分不因覆盖而失踪 —— 分数、最低维度、患者原话都写进了投诉单 content，追溯以投诉台账为准。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_survey_answer")
public class BizSurveyAnswer extends BaseEntity {

    /**
     * 满意线：李克特均分 >= 4（4-满意 / 5-非常满意）算满意
     */
    public static final BigDecimal SATISFIED_AVG = new BigDecimal("4.00");
    /**
     * 低分线：百分制 < 60 触发转投诉
     */
    public static final BigDecimal LOW_SCORE_100 = new BigDecimal("60.00");
    /**
     * 维度低分线：某维度均分 <= 2（含 1-非常不满意 / 2-不满意）触发转投诉
     */
    public static final BigDecimal LOW_DIMENSION_AVG = new BigDecimal("2.00");

    /**
     * 答卷编号（SV+yyyyMMdd+4位）
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
     * 李克特均分（1.00~5.00，只算计分题）
     */
    private BigDecimal avgScore;

    /**
     * 百分制得分 =（均分-1）/4*100（列名 score_100，数字不参与驼峰转换，必须显式映射）
     */
    @TableField("score_100")
    private BigDecimal score100;

    /**
     * NPS 推荐度（0-10，无 NPS 题为 null）
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
    private LocalDateTime fillTime;

    /**
     * 状态（1-有效 2-已作废）
     */
    private Integer answerStatus;

    /**
     * 低分自动转出的投诉单ID（幂等锚：一张答卷只转一次）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long disputeCaseId;
}
