package com.his.operation.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术登记单分页查询入参。
 */
@Data
public class DaySurgeryQueryPageDTO implements Serializable {

    /** 单号/患者姓名/术式名称模糊 */
    private String keyword;

    /** 状态:1-待评估 2-评估通过 3-已安排 4-术后观察 5-已出院 6-已取消 7-已转住院 */
    private Integer status;

    /** 准入术式 */
    private Long itemId;

    /** 科室ID */
    private Long deptId;

    /** 仅看在院（未终态：待评估/评估通过/已安排/术后观察） */
    private Boolean openOnly;

    /** 仅看超期（术后观察超最长滞留小时数，服务端判） */
    private Boolean overdueOnly;

    /** 计划手术起始日 yyyy-MM-dd */
    private String dateFrom;

    /** 计划手术截止日 yyyy-MM-dd */
    private String dateTo;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
