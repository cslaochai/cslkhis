package com.mis.common.base;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页入参基类
 */
@Data
public class PageParam implements Serializable {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public long getPageNum() {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    public long getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return pageSize > 200 ? 200 : pageSize;
    }
}
