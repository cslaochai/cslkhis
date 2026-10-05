package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 技术授权台账分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TechAuthQueryPageDTO extends PageParam {

    /**
     * 员工姓名（模糊）
     */
    private String employeeName;

    /**
     * 员工ID（从员工档案点进来看某人授权时传）
     */
    private Long employeeId;

    /**
     * 授权类别（1-手术 2-麻醉 3-内镜与介入）
     */
    private Integer authCategory;

    /**
     * 可独立操作的手术级别上限
     */
    private Integer techLevel;

    /**
     * 状态（1-待审批 2-已授权 3-已驳回 4-已收回）
     */
    private Integer authStatus;

    /**
     * 授权方式（1-独立授权 2-上级指导下 3-限制授权须上级在场）
     */
    private Integer authType;

    /**
     * 只看「今天有效」的授权（1=状态已授权且日期覆盖当天）。
     * 闸门回查与年度盘点都靠它，比筛状态少一次口径漂移。
     */
    private Integer onlyEffective;
}
