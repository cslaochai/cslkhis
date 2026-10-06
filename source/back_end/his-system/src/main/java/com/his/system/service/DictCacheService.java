package com.his.system.service;

import com.his.system.entity.SysDictData;
import java.util.List;

public interface DictCacheService {

    void refreshAllDictCache();

    void refreshDictCache(String dictType);

    List<SysDictData> getDictDataByType(String dictType);

    /**
     * 字典码值 → 文案（全工程翻译单点）。
     * 取不到渲染「未知(n)」，绝不回落成看似合法的值（回落会把脏数据伪装成正常数据）。
     */
    String getDicDataLabel(String dictType, Object value);

    void removeDictCache(String dictType);

    void clearAllDictCache();
}
