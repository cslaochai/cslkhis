package com.mis.common.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 经办前端分页返回（前端契约：{ list, total }）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> implements Serializable {

    private List<T> list;
    private long total;

    public static <T> PageVO<T> of(List<T> list, long total) {
        return new PageVO<>(list, total);
    }
}
