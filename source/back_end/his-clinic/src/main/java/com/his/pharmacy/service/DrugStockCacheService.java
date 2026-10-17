package com.his.pharmacy.service;

import com.his.pharmacy.entity.BizDrugStock;

public interface DrugStockCacheService {

    BizDrugStock getStockByDrugId(Long drugId);

    void putCache(Long drugId, BizDrugStock stock);

    void evictCache(Long drugId);

    void evictAllCache();
}
