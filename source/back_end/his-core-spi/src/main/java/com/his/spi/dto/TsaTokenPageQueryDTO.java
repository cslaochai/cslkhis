package com.his.spi.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 时间戳令牌台账分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TsaTokenPageQueryDTO extends PageParam {

    /**
     * 序列号（精确匹配，可空）
     */
    private String serial;

    /**
     * 摘要关键字（模糊，可空）
     */
    private String keyword;
}
