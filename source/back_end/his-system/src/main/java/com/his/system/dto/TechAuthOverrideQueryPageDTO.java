package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 技术越权登记分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TechAuthOverrideQueryPageDTO extends PageParam {

    /** 越权操作者姓名（快照） */
    private String employeeName;

    /** 授权类别（1-手术 2-麻醉 3-内镜与介入） */
    private Integer authCategory;

    /** 来源单据类型（1-手术申请 2-日间手术 3-住院医嘱 4-内镜记录） */
    private Integer sourceType;

    /** 状态（1-待上级确认 2-已确认） */
    private Integer overrideStatus;
}
