package com.his.system.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 时间戳令牌台账查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TsaTokenQueryPageDTO extends PageParam {

    /**
     * 序列号（精确匹配，可空）
     */
    private String serial;

    /**
     * 摘要前缀（模糊，可空）
     */
    private String keyword;
}
