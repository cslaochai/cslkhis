package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 科室主数据摘要：跨模块只读取科室的名称、类型、启用状态、上级科室与三类排班审核人。
 */
@Data
@NoArgsConstructor
public class DeptBriefVO {

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 科室编码
     */
    private String deptCode;

    /**
     * 上级科室ID（0=机构根；护理排班审核沿此逐级上溯到片区节点）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 科室位置（楼层 / 房间号，如「医技楼2楼」）；未维护时为空白，调用方不得自行编造楼层
     */
    private String location;

    /**
     * 科室联系电话；未维护时为空白
     */
    private String contactPhone;

    /**
     * 科室类型（1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他），多个类型逗号分隔
     */
    private String deptType;

    /**
     * 节点类型（0-医疗机构 1-管理片区 2-临床科室 3-医技科室 4-药剂科室 5-行政后勤 6-医疗辅助 7-其他）
     * <p>排班单元、开诊科室等「必须是真人上班的地方」按它过滤，片区与行政后勤不能入选。
     */
    private String nodeType;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 门诊排班审核人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long outpatientAuditorId;

    /**
     * 护理排班审核人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nursingAuditorId;

    /**
     * 全院岗位排班审核人（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scheduleAuditorId;
}
