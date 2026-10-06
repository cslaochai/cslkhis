package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 心电工作台分页查询（sql/173）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EcgQueryPageDTO extends PageParam {

    /**
     * 关键字：患者姓名 / 患者号 / 记录号 / 检查项目名
     */
    private String keyword;

    /**
     * 只看「待采集」（记录状态 1已登记 / 2已签到 / 3检查中）
     */
    private Boolean collectPending;

    /**
     * 只看「已采集还没写报告」的检查（report_id IS NULL 且状态 >= 4）
     */
    private Boolean onlyUnwritten;

    /**
     * 报告状态（0-草稿 1-待审核 3-已审核 4-已发布 5-已作废）。
     * 传 null = 不过滤。
     */
    private Integer reportStatus;

    /**
     * 起始日期（yyyy-MM-dd，按检查记录创建时间）
     */
    private String startDate;

    /**
     * 截止日期（yyyy-MM-dd；服务端补 23:59:59，不补会把当天全部时点滤掉）
     */
    private String endDate;
}
