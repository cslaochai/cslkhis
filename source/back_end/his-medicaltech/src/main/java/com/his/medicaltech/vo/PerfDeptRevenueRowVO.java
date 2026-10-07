package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 科室月度收入（{@code PerfMapper#sumDeptRevenue}）。
 *
 * <p>收入 = L1 费用记账流水月度净额（收正退负 SUM 现算，按记账行所属科室归属，
 * 账务归属月看 book_time 而非 create_time）。
 *
 * <p>SQL 刻意<b>不 GROUP BY</b>：同一dept_id 若有多份科室名快照，分组会返回多行，
 * 调用方按单行接收会直接炸。取 {@code MAX(dept_name)} 是"随便挑一个能看的名字"的诚实写法。
 */
@Data
public class PerfDeptRevenueRowVO implements Serializable {

    /**
     * 科室名称快照（该科室没有流水时为「未分配科室」）
     */
    private String deptName;

    /**
     * 月度收入净额
     */
    private BigDecimal revenue;

    /**
     * 药品收入净额（item_type 2西药 3中成药 4中药饮片）
     */
    private BigDecimal drugRevenue;
}