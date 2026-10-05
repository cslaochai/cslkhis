package com.his.common.base;

import lombok.Data;

/**
 * 分页查询基类
 */
@Data
public class PageParam {

    /**
     * 页码
     */
    private int pageNum = 1;

    /**
     * 每页条数
     */
    private int pageSize = 10;
}
