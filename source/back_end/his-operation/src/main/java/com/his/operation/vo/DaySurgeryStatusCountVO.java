package com.his.operation.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术按状态分组的登记单数（{@code BizDaySurgeryApplyMapper#countByStatus} 的返回行）。
 *
 * <p>对应 SQL：{@code biz_day_surgery_apply} 按 {@code status} 的 group by 计数，
 * 供 {@code DaySurgeryServiceImpl#stat()} 展开成 {@link DaySurgeryStatVO} 的七个状态档。
 *
 * <p>SQL 别名用 {@code cnt} 而不是 {@code count}：后者在 {@code ORDER BY} 目标位置
 * 容易被当成聚合函数关键字，MyBatis 映射时也不该让列名与 SQL 语义撞车。
 */
@Data
public class DaySurgeryStatusCountVO implements Serializable {

    /**
     * 登记单状态（1-待评估 2-评估通过 3-已排台 4-术后观察 5-已离院 6-已取消 7-已转住院）
     */
    private Integer status;

    /**
     * 该状态的登记单数
     */
    private Long cnt;
}
