package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * 订餐配送分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MealOrderQueryPageDTO extends PageParam {

    /**
     * 就餐日期（单日，最常用）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate mealDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate beginDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 病区ID
     */
    private Long wardId;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 入院ID
     */
    private Long admissionId;

    /**
     * 来源膳食方案ID
     */
    private Long dietPlanId;

    /**
     * 配餐状态（0-待配餐 1-已配餐 2-已配送 3-已签收 4-已取消）
     */
    private Integer deliverStatus;

    /**
     * 餐次（1-早餐 2-午餐 3-晚餐 4-加餐）
     */
    private Integer mealType;

    /**
     * 饮食类型码
     */
    private String dietCode;

    /**
     * 关键字：患者姓名 / 患者编号 / 住院号 / 订餐单号
     */
    private String keyword;

    /**
     * 服务端填入的科室数据权限集合
     */
    private List<Long> scopeDeptIds;
}
