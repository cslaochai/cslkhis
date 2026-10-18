package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 纠纷/投诉统计（服务端 group by 出）。
 */
@Data
public class DisputeStatVO implements Serializable {

    /**
     * 总条数
     */
    private Long total;

    /** 待受理条数（纠纷状态 1-待受理） */
    private Long pendingCount;

    /** 调查中条数（纠纷状态 2-调查中） */
    private Long investigatingCount;

    /** 处理中条数（纠纷状态 3-处理中） */
    private Long handlingCount;

    /** 已结案条数（纠纷状态 4-已结案） */
    private Long closedCount;

    /** 已撤销条数（纠纷状态 5-已撤销） */
    private Long revokedCount;

    /**
     * 未结案合计（待受理+调查中+处理中）
     */
    private Long openCount;

    /**
     * 已结案单据赔偿合计（元）
     */
    private BigDecimal compensationTotal;

    /**
     * 平均结案天数（受理→结案，服务端算，未结案不参与）
     */
    private BigDecimal avgCloseDays;

    /**
     * 按类型分布（key=码值，name=字典文案由前端渲染，count=条数）
     */
    private List<DisputeStatItemVO> byCaseType;

    /**
     * 被投诉科室 TOP（count 倒序前 10）
     */
    private List<DisputeStatItemVO> byDeptTop;
}
