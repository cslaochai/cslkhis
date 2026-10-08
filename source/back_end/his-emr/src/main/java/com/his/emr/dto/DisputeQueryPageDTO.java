package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 纠纷/投诉分页查询入参（listPage 为 POST）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DisputeQueryPageDTO extends PageParam implements Serializable {

    /**
     * 单号/患者姓名/投诉人模糊
     */
    private String keyword;

    /**
     * 类型:1-服务投诉 2-医疗纠纷 3-医疗损害争议 4-其他
     */
    private Integer caseType;

    /**
     * 状态:1-待受理 2-调查中 3-处理中 4-已结案 5-已撤销
     */
    private Integer status;

    /**
     * 等级:1-一般 2-较大 3-重大
     */
    private Integer level;

    /**
     * 被投诉科室
     */
    private Long deptId;

    /**
     * 仅看未结案（待受理+调查中+处理中）
     */
    private Boolean openOnly;

    /**
     * 登记起始日期 yyyy-MM-dd
     */
    private String dateFrom;

    /**
     * 登记截止日期 yyyy-MM-dd
     */
    private String dateTo;
}