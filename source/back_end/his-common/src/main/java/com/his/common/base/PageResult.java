package com.his.common.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 */
@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    private long total;
    private long pageNum;
    private long pageSize;
    private long pages;
    private List<T> records;

    public PageResult(long total, long pageNum, long pageSize, long pages, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pages;
        this.records = records;
    }

    public static <T> PageResult<T> of(long total, long pageNum, long pageSize, long pages, List<T> records) {
        return new PageResult<>(total, pageNum, pageSize, pages, records);
    }
}
