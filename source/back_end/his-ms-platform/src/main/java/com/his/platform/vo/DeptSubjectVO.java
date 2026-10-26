package com.his.platform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 科室 → 诊疗科目 对照结果。
 */
@Data
public class DeptSubjectVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    private String deptCode;

    private String deptName;

    /**
     * 科室类型（逗号多值，1门诊 2医技 3药房 4住院 5其他 6急诊）
     */
    private String deptType;

    /**
     * 上级科室名称（分组目录节点为空）
     */
    private String parentName;

    /**
     * 上报主科目代码（没配就是空，病案首页上报会取不到值）
     */
    private String primarySubjectCode;

    /**
     * 上报主科目名称
     */
    private String primarySubjectName;

    /**
     * 该科室对应的全部科目
     */
    private List<DeptSubjectItemVO> subjects;
}
