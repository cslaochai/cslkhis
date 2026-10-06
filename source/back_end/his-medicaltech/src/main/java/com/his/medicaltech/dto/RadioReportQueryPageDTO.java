package com.his.medicaltech.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 放射诊断工作台分页查询（sql/138）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RadioReportQueryPageDTO extends PageParam {

    /** 关键字：患者姓名 / 患者号 / 记录号 / 检查项目名 */
    private String keyword;

    /**
     * 报告状态（0-草稿 1-待审核 3-已审核 4-已发布 5-已作废）。
     * 传 null = 不过滤；要筛「还没写报告」用 {@link #onlyUnwritten}。
     */
    private Integer reportStatus;

    /** 只看还没人写报告的检查（report_id IS NULL） */
    private Boolean onlyUnwritten;

    /** 阴阳性（字典 his_positive_flag） */
    private Integer positiveFlag;

    /** 起始日期（yyyy-MM-dd，按检查记录创建时间） */
    private String startDate;

    /** 截止日期（yyyy-MM-dd；服务端补 23:59:59，不补会把当天全部时点滤掉） */
    private String endDate;
}
