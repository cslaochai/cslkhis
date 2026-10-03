package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 剂量上限知识分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DoseLimitQueryPageDTO extends PageParam {

    /** 成分关键字（模糊匹配 component） */
    private String component;

    /** 口径说明关键字（模糊匹配 note） */
    private String keyword;

    /** 状态过滤（1-启用 0-停用），NULL=全部 */
    private Integer status;
}
