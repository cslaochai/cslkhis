package com.his.platform.service;

import com.his.platform.entity.SysDictData;

import java.util.List;

/**
 * 数据字典缓存服务：字典行按类型缓存在 Redis，全工程「编码→文案」与字典下拉取数的单一出口。
 * 内部完整版；对外窄端口见 {@link com.his.spi.service.DictCacheService}。
 */
public interface DictCacheService {

    /**
     * 全量重建字典缓存（按类型分组逐个写入，单类型写失败不中断其余）。
     * 服务启动装配时预热一次；手动刷新全量字典缓存时也复用这一个入口。
     */
    void refreshAllDictCache();

    /**
     * 回源重载单个字典类型的缓存。
     * 字典数据在字典管理页新增/修改/删除后立即调用，保证操作员改完即生效，不等缓存过期。
     */
    void refreshDictCache(String dictType);

    /**
     * 取一个字典类型下的整组字典行（按排序号），缓存未命中则回库并回填。
     * 内部使用，返回完整实体；对外窄端口 {@code getDictDataByType} 只回 DictItemVO。
     */
    List<SysDictData> getDictDataByTypeRaw(String dictType);

    /**
     * 字典码值 → 文案（全工程字典翻译单点）。
     * 类型编码只许用 {@code com.his.common.constant.DictType} 的常量，不许在调用点写字面量。
     * 取不到（字典没这个类型、或这个码值没行）返回空串，与枚举 {@code getText} 同口径；
     * 要保留原始脏码值排查是枚举 {@code getLabelOrUnknown} 的事，字典侧不伪造文案。
     */
    String getDicDataLabel(String dictType, Object value);

    /**
     * 丢弃单个字典类型的缓存条目（只删键，不回源重建）。
     * 用于该类型数据已变更但当下不必立刻预热的场合，下次读取自然回库回填。
     */
    void removeDictCache(String dictType);

    /**
     * 清空全部字典缓存键。
     * 用于字典结构整体调整后的清场：不留任何旧键，后续读取逐个回源重建。
     */
    void clearAllDictCache();
}
