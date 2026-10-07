package com.his.ai.vo;

import lombok.Data;

/**
 * 检验结果表按项目名聚合的一行（白话词典覆盖率统计用）。
 *
 * <p>对应 {@code biz_lab_result} 的 {@code SELECT laboratory_item_name, COUNT(*) ... GROUP BY laboratory_item_name}，
 * 别名与字段名严格一致（见 {@code LabResultRefCountMapper#countGroupByItemName}）。
 *
 * <p>建这个 VO 的原因：原实现用 {@code QueryWrapper + selectMaps} 拿裸Map，
 * 键名 {@code ref_cnt} 只在字符串里出现过一次，改列名不会有编译错误、
 * 只会让 {@code refCount} 静默变 0（覆盖率报表少算，维护页看不出异常）。
 */
@Data
public class LabItemRefCountVO {

    /**
     * 检验项目名称
     */
    private String laboratoryItemName;

    /**
     * 该项目名在检验结果表中出现的次数
     */
    private Long refCount;
}