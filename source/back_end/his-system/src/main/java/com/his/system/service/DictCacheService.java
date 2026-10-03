package com.his.system.service;

import org.springframework.boot.CommandLineRunner;
import com.his.system.entity.SysDictData;
import java.util.List;

public interface DictCacheService extends CommandLineRunner {

    void run(String... args);

    void refreshAllDictCache();

    void refreshDictCache(String dictType);

    List<SysDictData> getDictDataByType(String dictType);

    void removeDictCache(String dictType);

    void clearAllDictCache();
}
