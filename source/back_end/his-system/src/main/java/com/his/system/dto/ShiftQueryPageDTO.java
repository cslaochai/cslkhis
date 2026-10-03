package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班次字典分页查询入参（关键词=名称模糊）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftQueryPageDTO extends PageParam {

    /**
     * 关键词（班次名称模糊匹配）
     */
    private String keyword;

    /**
     * 状态：0-停用 1-启用（空=全部）
     */
    private Integer status;

    /**
     * 适用域：1-门诊排班 2-病区护理（空=全部；班次字典页用它分册展示）
     */
    private Integer useScope;
}
