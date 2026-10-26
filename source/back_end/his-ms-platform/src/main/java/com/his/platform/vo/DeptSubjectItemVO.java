package com.his.platform.vo;

import lombok.Data;

/**
 * 科室已映射的单个诊疗科目。
 */
@Data
public class DeptSubjectItemVO {

    /**
     * 科目代码
     */
    private String subjectCode;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 上级科目名称（一级科目为空），列表里拼成「内科 / 呼吸内科专业」更好认
     */
    private String parentName;

    /**
     * 是否该科室的上报主科目：1-是 0-否
     */
    private Integer isPrimary;
}
