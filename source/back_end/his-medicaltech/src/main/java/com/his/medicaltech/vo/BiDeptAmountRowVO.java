package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 科室收入 TOP5 一行（{@code BiMapper#deptTop}）。
 *
 * <p>分组用的是记账行上的科室名<b>快照</b>而非科室表：科室改名后历史流水仍记旧名，
 * 这样"改名前后的收入"能被连起来看（代价是同一科室若曾改名会拆成两行，属刻意取舍）。
 */
@Data
public class BiDeptAmountRowVO implements Serializable {

    /**
     * 科室名称（未分配时为「未分配科室」）
     */
    private String deptName;

    /**
     * 近 30 日收入净额
     */
    private BigDecimal amount;
}