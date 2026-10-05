package com.his.common.dto;

import lombok.Data;

/**
 * 时间戳令牌台账查询入参。
 */
@Data
public class TsaTokenQueryPageDTO {

    /**
     * 序列号（精确匹配，可空）
     */
    private String serial;

    /**
     * 摘要前缀（模糊，可空）
     */
    private String keyword;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 20;
}
