package com.mss.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 经办前端分页入参基类（前端契约：page / size / keyword）
 */
@Data
public class BizPageParam implements Serializable {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;

    public long getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public long getSize() {
        if (size == null || size < 1) {
            return 10;
        }
        return size > 200 ? 200 : size;
    }
}
