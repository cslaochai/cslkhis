package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 传染病报告卡分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InfectiousReportQueryPageDTO extends PageParam {

    /**
     * 状态
     */
    private Integer reportStatus;

    /**
     * 传染病类别（快照，1甲/2乙/3丙）
     */
    private Integer infectiousClass;

    /**
     * 单号/患者姓名/病种关键字
     */
    private String keyword;

    /**
     * 逾期未报筛选（1-只看超时未报：待审核且已过时限）
     */
    private Integer overdue;

    /**
     * 填卡起始
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTimeStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTimeEnd;
}
