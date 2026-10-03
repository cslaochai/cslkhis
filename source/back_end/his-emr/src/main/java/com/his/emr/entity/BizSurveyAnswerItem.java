package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 满意度逐题答案（sql/164）。
 *
 * <p><b>dimension/seqNo/title 是题目快照</b>：模板改题干、换维度，历史答卷若只存 item_id，
 * 去年的分数就在新维度名下查不到 —— 等于把历史统计洗没了，而报表不会报错。
 *
 * <p>uk_survey_answer_item(answer_id,item_id) 不含 del_flag：重算只能整卷作废重填，
 * 若确需覆盖本题答案，走 {@code purgeByAnswer} 物理删明细再写。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_survey_answer_item")
public class BizSurveyAnswerItem extends BaseEntity {

    /** 答卷ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long answerId;

    /** 题目ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /** 模板ID（冗余，按模板聚合时免去 join） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /** 评价维度（题目快照） */
    private Integer dimension;

    /** 题号（快照） */
    private Integer seqNo;

    /** 题干（快照） */
    private String title;

    /** 题型（快照）:1-量表 2-单选 3-多选 4-NPS 5-开放文本 */
    private Integer questionType;

    /** 得分：量表 1-5，NPS 0-10；单选/多选/文本为 null */
    private Integer score;

    /** 选项文本（单选/多选，多选取分号拼接） */
    private String optionLabel;

    /** 文本题回答 */
    private String textValue;
}
