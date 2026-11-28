package com.his.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 科室下拉选择入参 —— 所有「科室下拉」的统一入口。
 */
@Data
public class DepartmentSelectDTO {

    /**
     * 数据范围：
     * <ul>
     *   <li>{@code null} / 未传 → <b>按当前人过滤</b>（默认，最安全）；
     *       不限权用户（{@code data_scope=1}）此时等于全部科室</li>
     *   <li>{@code CURRENT}（或不区分大小写的 current）→ 显式要求按当前人过滤</li>
     *   <li>{@code ALL}（或不区分大小写的 all）→ 显式要求全部科室，
     *       一般用于字典维护 / 排班模板这类需要全量范围的场景</li>
     * </ul>
     *
     * <p><b>默认值刻意是「按当前人过滤」</b>：漏传参数的页面拿到的是收窄结果，
     * 而不是全院号源 —— 出错的方向必须是"看不到"而不是"看到太多"。
     */
    @Schema(description = "数据范围：CURRENT-按当前人（默认） ALL-全部科室")
    private String scope;

    /**
     * 科室类型过滤（支持单个值或逗号分隔的多个值）。
     * 口径见科室的科室类型列注释与字典 {@code his_dept_type}。
     */
    @Schema(description = "科室类型：1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他")
    private String deptType;

    /**
     * 排除的科室类型（逗号分隔）：命中即剔除。用于「普通排班排除急诊」这类
     * 反向场景——包含用 {@link #deptType}，排除用本字段，不再在调用方内存过滤。
     */
    @Schema(description = "排除科室类型逗号串，命中即剔除")
    private String excludeDeptType;

    /**
     * 仅出对外开放挂号的科室（{@code is_open=1} 且启用），供小程序挂号目录等场景。
     */
    @Schema(description = "true-只出对外开放挂号科室")
    private Boolean openOnly;

    /**
     * 节点类型过滤（单值）。
     * <p>排班单元、开诊科室、收治科室这类「必须落到真人上班的地方」的下拉要传
     * {@code 2,3,4,6,7}（见 {@code DeptNodeTypeEnum#isBusinessDept}）把管理片区与行政后勤挡在外面；
     * 岗位配置这类需要选「片区」的下拉不传，靠出参的 nodeType 自行标注。
     */
    @Schema(description = "节点类型：1-管理片区 2-临床科室 3-医技科室 4-药剂科室 5-行政后勤 6-医疗辅助 7-其他")
    private String nodeType;

    /**
     * 树形下拉选择模式（{@code selectTree} 入口使用）：
     * <ul>
     *   <li>{@code LEAF} — 仅末级业务科室可选；</li>
     *   <li>{@code TREE}（默认）— 父子均可，父节点按子树聚合；</li>
     *   <li>{@code ORG} — 组织归属维护，除根节点外均可选。</li>
     * </ul>
     * 码值见 {@code DeptSelectModeEnum}。
     */
    @Schema(description = "选择模式：LEAF-仅末级 TREE-父子均可 ORG-组织归属维护，默认 TREE")
    private String mode;
}
