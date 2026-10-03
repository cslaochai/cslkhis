package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.emr.entity.BizSurveyItem;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 满意度问卷模板（详情带题目清单）。
 */
@Data
public class SurveyTemplateVO implements Serializable {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 模板编号 */
    private String templateNo;

    /** 问卷名称 */
    private String templateName;

    /** 适用场景（1-出院随访 2-门诊 3-住院在院 4-体检） */
    private Integer scene;

    /** 状态（1-启用 2-停用） */
    private Integer status;

    /** 说明（调查目的、口径、上报去向） */
    private String description;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 题目数：列表页那一列的事实来源，由 listPage 批量 count 回填（不在库里冗余存数） */
    private Integer itemCount;

    /** 题目（列表接口不返回，详情与编辑回显返回） */
    private List<BizSurveyItem> items;
}
