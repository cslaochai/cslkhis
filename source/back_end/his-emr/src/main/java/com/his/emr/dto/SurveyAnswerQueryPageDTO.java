package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 答卷分页入参。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SurveyAnswerQueryPageDTO extends PageParam implements Serializable {

    /**
     * 答卷号/患者姓名模糊
     */
    private String keyword;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 场景（快照）
     */
    private Integer scene;

    /**
     * 状态:1-有效 2-已作废（默认只看有效由前端控制，不传=全部）
     */
    private Integer answerStatus;

    /**
     * 仅看低分卷（百分制<60 或某维度均分<=2）
     */
    private Boolean lowScoreOnly;

    /**
     * 填报方式（1-患者自填 2-随访员代填 3-现场扫码）
     */
    private Integer fillSource;

    /**
     * 提交起始日期 yyyy-MM-dd
     */
    private String dateFrom;

    /**
     * 提交截止日期 yyyy-MM-dd
     */
    private String dateTo;

    /**
     * 就诊科室ID
     */
    private Long deptId;

    /**
     * 服务端收口的科室集合（null=不受限）
     */
    private List<Long> scopeDeptIds;
}
