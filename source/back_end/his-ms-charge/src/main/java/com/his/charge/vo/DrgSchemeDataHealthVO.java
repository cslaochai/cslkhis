package com.his.charge.vo;

import lombok.Data;

/**
 * DRG 方案数据健康度：用一条 SQL 汇总「启用行数 / 层级一致性 / 排除组引用」三类硬指标。
 *
 * <p>这些指标不是业务数据，而是「目录本身能不能用」的证据：
 * <ul>
 *   <li>{@code *Enabled} 全为 0 说明导入时漏置 status=1，引擎只会安静地判 0000；</li>
 *   <li>{@code groupMdcMismatch} 是组表反规范化列与 ADRG 所属 MDC 的冲突数；</li>
 *   <li>{@code missingExclSets} 是 ccmcc 引用了 set 表里不存在的排除组编号。</li>
 * </ul>
 */
@Data
public class DrgSchemeDataHealthVO {

    /**
     * 启用的 MDC 行数
     */
    private Integer mdcEnabled;

    /**
     * 启用的 ADRG 行数（含 QY/占位伪行）
     */
    private Integer adrgEnabled;

    /**
     * 启用的 DRG 行数（含 QY/占位伪行）
     */
    private Integer groupEnabled;

    /**
     * 启用的 CC/MCC 行数
     */
    private Integer ccmccEnabled;

    /**
     * 启用的码集合行数
     */
    private Integer setEnabled;

    /**
     * 组表 mdc_code 与所属 ADRG 的 mdc_code 不一致的行数
     */
    private Integer groupMdcMismatch;

    /**
     * ccmcc.excl_group 指向了 biz_drg_set 中不存在的排除组编号个数
     */
    private Integer missingExclSets;
}
