package com.his.platform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * 科室树形下拉出参：每个节点自带可选标记，前端只按 {@link #disabled} 渲染灰显，不自行判定业务规则。
 */
@Data
public class DepartmentTreeSelectVO {

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
     * 科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔
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
     * 祖先路径（自根到当前节点，斜杠拼接，如「内科系统 / 神经内科」）。
     * 平铺下拉用整行路径展示与同名消歧；树形下拉不消费本字段。
     */
    private String fullPath;

    /**
     * 是否不可选：true 时前端灰显，仅作分组展示
     */
    private Boolean disabled;

    /**
     * 不可选原因（disabled=true 时返回，供 hover 提示）
     */
    private String disabledReason;

    /**
     * 子科室
     */
    private List<DepartmentTreeSelectVO> children;
}
