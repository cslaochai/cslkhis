package com.his.iot.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 设备共享调配申请单分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceRentalPageQueryDTO extends PageParam implements Serializable {

    /** 状态：1-待审批 2-待发放 3-使用中 4-待验收 5-已完成 6-已取消 7-已驳回，空为全部 */
    private Integer status;

    /** 单号/患者/床位/申请人/科室模糊 */
    private String keyword;
}
