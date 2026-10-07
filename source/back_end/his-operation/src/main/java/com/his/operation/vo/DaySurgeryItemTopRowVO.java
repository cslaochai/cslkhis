package com.his.operation.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术术式分布 TOP10 的一行（{@code BizDaySurgeryApplyMapper#countByItemTop} 的返回行）。
 *
 * <p>对应 SQL：{@code biz_day_surgery_apply} 按 {@code (item_id, item_name)} 的 group by
 * 计数，倒序取前 10。术式名缺失时 SQL 侧兜底成「未知术式」——
 * 否则「有人登记过、但没选术式」的单子会归到 {@code NULL} 分组里从榜单上凭空消失。
 *
 * <p>与出参 {@link DaySurgeryItemCountVO} 分开：这一行是<b>SQL 原始行</b>，
 * 字段名跟 SQL 别名对齐；出参那一份是给前端的成品（术式 ID 要转字符串防雪花精度丢失）。
 */
@Data
public class DaySurgeryItemTopRowVO implements Serializable {

    /**
     * 术式ID（可能为 NULL —— 历史单据没录术式）
     */
    private Long itemId;

    /**
     * 术式名称快照（缺失时为「未知术式」）
     */
    private String itemName;

    /**
     * 该术式的登记单数
     */
    private Long cnt;
}
