package com.his.platform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 科室下拉统一入口出参。
 */
@Data
public class DepartmentSelectListVO {

    /**
     * 科室ID（序列化成字符串，避免 JS 精度丢失）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 科室编码（唯一）
     */
    private String deptCode;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 科室类型：1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他，多个类型逗号分隔
     * （口径见列注释与字典 {@code his_dept_type}）
     */
    private String deptType;

    /**
     * 节点类型（1-管理片区 2-临床科室 3-医技科室 4-药剂科室 5-行政后勤 6-医疗辅助 7-其他）
     */
    private String nodeType;

    /**
     * 节点类型文案（枚举 DeptNodeTypeEnum 翻译）
     */
    private String nodeTypeText;

    /**
     * 上级科室ID，顶级为 0
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    /**
     * 上级科室名称（用于下拉展示层级关系）
     */
    private String parentDeptName;
}
