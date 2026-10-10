package com.his.patient.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 等床队列查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BedWaitQueryPageDTO extends PageParam {

    /**
     * 状态：0-等待中 1-已安排床位 2-已收治 3-已取消，null=全部
     */
    private Integer waitStatus;

    /**
     * 拟收治科室ID
     */
    private Long applyDeptId;

    /**
     * 优先级（1-普通 2-急 3-危重）
     */
    private Integer priority;

    /**
     * 需求床型：normal / ICU / VIP
     */
    private String bedType;

    /**
     * 患者姓名 / 等待号 / 电话模糊查询
     */
    private String keyword;

    /**
     * 只看已等待超时（超过最长等待天数）的
     */
    private Boolean overdueOnly;
}
