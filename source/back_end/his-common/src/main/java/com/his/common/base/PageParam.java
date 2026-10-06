package com.his.common.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 分页查询基类。
 *
 * <p>越界用getter 夹取（不抛异常）而不是 @Min/@Max 报 400：前端本来就合法地传 200，
 * 导出还要一次拉几千行，硬校验会把导出和超档请求一起打回。越界静默夹到上限。
 */
@Data
public class PageParam {

    /**
     * 默认每页条数
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 外部入参允许的每页条数上限（防止前端传 100000 把库拖死）
     */
    public static final int MAX_PAGE_SIZE = 200;

    /**
     * 页码，小于 1 一律按第 1 页
     */
    private int pageNum = 1;

    private int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 导出模式（true = 放开 {@link #MAX_PAGE_SIZE} 上限，一次拉全量）。
     *
     * <p>{@code transient} + {@code @JsonIgnore} 双保险：前端请求体反序列化时忽略它，
     * 响应里也不会带出去 —— 这个开关只能由服务端 {@link #forExport(int)} 打开。
     * 前端传 200 是合法的，但导出要一次拉几千行，走这个通道才不会被夹回 200。
     */
    @JsonIgnore
    private transient boolean exportMode;

    public int getPageNum() {
        return pageNum < 1 ? 1 : pageNum;
    }

    public int getPageSize() {
        if (pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return exportMode ? pageSize : Math.min(pageSize, MAX_PAGE_SIZE);
    }

    /**
     * 导出绕过通道：置导出模式、固定第 1 页、按 maxRows 放开每页上限。
     */
    public void forExport(int maxRows) {
        this.exportMode = true;
        this.pageNum = 1;
        this.pageSize = maxRows;
    }
}
