package com.his.system.dto;

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
     * 科室名称，模糊匹配（下拉搜索用）
     */
    @Schema(description = "科室名称，模糊匹配")
    private String deptName;
}
