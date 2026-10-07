package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 科室归集聚合（一个科室一行），对应
 * {@code BizDaySettlementMapper#sumDetailByDept}。
 *
 * <p>SQL 形态：{@code SELECT i.dept_id AS deptId, MAX(i.dept_name) AS deptName,
 * COUNT(*) AS cnt, COALESCE(SUM(i.amount), 0) AS amount ... GROUP BY i.dept_id}。
 *
 * <p><b>科室锚点取摊行自己的 dept_id</b>（出账时从记账行带过来），所以这张表按
 * 「哪科室开的费用」计，与谁收的钱无关；{@code dept_name} 用 {@code MAX()} 兜底 ——
 * 同一科室的行里只要有一行带了名称就能显示，撤科后也不会变成一片空白。
 */
@Data
public class DeptAmountSumVO implements Serializable {

    /**
     * 科室ID（SQL 已过滤掉 NULL，无科室归属的行不进这里）
     */
    private Long deptId;

    /**
     * 科室名称（取组内MAX，摊行未带名称时为 null）
     */
    private String deptName;

    /**
     * 摊行笔数（{@code COUNT(*)}；MySQL BIGINT）
     */
    private Long cnt;

    /**
     * 摊行金额合计（元，毛收入，未扣优惠/统筹）
     */
    private BigDecimal amount;
}