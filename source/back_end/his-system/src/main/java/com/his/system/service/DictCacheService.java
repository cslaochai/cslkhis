package com.his.system.service;

import com.his.system.entity.SysDictData;
import java.util.List;

public interface DictCacheService {

    void refreshAllDictCache();

    void refreshDictCache(String dictType);

    List<SysDictData> getDictDataByType(String dictType);

    /**
     * 字典码值 → 文案（全工程字典翻译单点）。
     * 类型编码只许用 {@code com.his.common.constant.DictType} 的常量，不许在调用点写字面量。
     * 取不到（字典没这个类型、或这个码值没行）返回空串，与枚举 {@code getText} 同口径；
     * 要保留原始脏码值排查是枚举 {@code labelOrUnknown} 的事，字典侧不伪造文案。
     */
    String getDicDataLabel(String dictType, Object value);

    void removeDictCache(String dictType);

    void clearAllDictCache();
}
