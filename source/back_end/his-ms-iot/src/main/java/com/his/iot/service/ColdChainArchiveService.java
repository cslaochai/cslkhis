package com.his.iot.service;

/**
 * 冷链 GSP 归档：把 TDengine 的 5 分钟聚合落到 biz_coldchain_temp_log，供 5 年留档与检查导出。
 */
public interface ColdChainArchiveService {

    /**
     * 归档已完成的 5 分钟窗口。以归档表 MAX(ts) 为游标，重跑只补缺口；首次最多回补 24 小时。
     *
     * @return 本次实际写入行数
     */
    int archiveRecent();
}
