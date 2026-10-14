package com.mis.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 经办前端分页入参基类
 * 兼容两套口径：page/size（MSS 自研）与 pageNum/pageSize（HIS 契约），getPage/getSize 自动归一
 */
@Data
public class BizPageParam implements Serializable {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    /** HIS 口径：页码（优先于 page） */
    private Integer pageNum;
    /** HIS 口径：每页条数（优先于 size） */
    private Integer pageSize;

    public long getPage() {
        Integer p = pageNum != null ? pageNum : page;
        return p == null || p < 1 ? 1 : p;
    }

    public long getSize() {
        Integer s = pageSize != null ? pageSize : size;
        if (s == null || s < 1) {
            return 10;
        }
        return s > 200 ? 200 : s;
    }
}
