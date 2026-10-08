package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 发放/回收台账分页入参。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SurveyDispatchQueryPageDTO extends PageParam implements Serializable {

    /**
     * 单号/患者姓名模糊
     */
    private String keyword;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 发放来源（1-随访任务 2-出院结算 3-人工补发）
     */
    private Integer sourceType;

    /**
     * 回收状态（1-待推送 2-已推送待回收 3-已回收 4-已过期 5-已拒答）
     */
    private Integer dispatchStatus;

    /**
     * 回收渠道（1-电话代填 2-短信 3-微信 4-现场扫码）
     */
    private Integer channel;

    /**
     * 仅看超截止未回收
     */
    private Boolean overdueOnly;

    /**
     * 发放起始日期 yyyy-MM-dd
     */
    private String dateFrom;

    /**
     * 发放截止日期 yyyy-MM-dd
     */
    private String dateTo;

    /**
     * 显式指定科室（越权直接报错，不静默改写）
     */
    private Long deptId;

    /**
     * 服务端收口的科室集合（null=不受限）
     */
    private List<Long> scopeDeptIds;
}
